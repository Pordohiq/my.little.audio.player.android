package my.little.audio.player.android.ResTree;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.nio.file.*;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.documentfile.provider.DocumentFile;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.Signals;

public class ResTree {
	public static Uri library_root;
	@Nullable
	public static List<DiskElement> library;
	public static List<DiskElement> current_folder;
	
	public static void init(Context context) {
		library_root = read_path_config(context);
		if (library_root != null) {
			Log.i(Global.APP_TAG, "Library root: " + library_root);
			library = load_library(library_root);
			current_folder = library;
		}
	}
	
	public static void reload_from_disk(Context context) {
		Log.v(Global.APP_TAG, "Reloading library path from disk");
		library_root = read_path_config(context);
		Log.v(Global.APP_TAG, "Reloading library tree from disk");
		library = null;
		library = load_library(library_root);
		current_folder = library;
	}
	
	public static void set_library_path(@NonNull Uri uri) {
		Log.w(Global.APP_TAG, "Setting library path: " + uri);
		DocumentFile root = DocumentFile.fromTreeUri(Global.getInstance(), uri);
		
		if (root != null && root.exists() && root.isDirectory()) {
			Log.v(Global.APP_TAG, "Confirmed valid directory URI: " + uri);
			
			write_path_config(Global.getInstance(), uri);
			
			ResTree.library_root = uri;
			Global.setPath(new ArrayList<>());
			
			library = load_library(uri);
			current_folder = library;
		}
		else {
			Log.w(Global.APP_TAG, "Invalid URI or directory not found: " + uri);
		}
	}
	
	@Nullable
	private static Uri read_path_config(@NonNull Context context) {
		File filesDir = context.getFilesDir();
		
		if (filesDir == null) {
			return null;
		}
		
		File pathConfigFile = new File(filesDir, "library.path");
		String content = "";
		
		try {
			if (pathConfigFile.exists()) {
				byte[] bytes = Files.readAllBytes(pathConfigFile.toPath());
				content = new String(bytes, StandardCharsets.UTF_8).trim();
			}
		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error reading path config");
			return null;
		}
		
		if (!content.isEmpty()) {
			try {
				Uri uri = Uri.parse(content);
				DocumentFile root = DocumentFile.fromTreeUri(context, uri);
				if (root != null && root.exists()) {
					return uri;
				}
			} catch (IllegalArgumentException iaex) {
				Log.w(Global.APP_TAG, "Invalid URI found in config: " + content);
				return null;
			}
		}
		
		Log.w(Global.APP_TAG, "No valid URI found in config.");
		return null;
	}
	
	private static void write_path_config(@NonNull Context context, Uri newPath) {
		Log.v(Global.APP_TAG, "Writing path config: " + newPath);
		File filesDir = context.getFilesDir();
		if (filesDir == null) return;
		
		Path pathConfig = filesDir.toPath().resolve("library.path");
		
		try {
			Files.write(pathConfig, newPath.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error writing path config, new path: " + newPath);
		}
	}
	
	//region Direct ResTree interactions
	@Nullable
	public static List<DiskElement> load_library(@NonNull Uri uri) {
		List<DiskElement> elements = new ArrayList<>();
		ContentResolver resolver = Global.getInstance().getContentResolver();
		Uri childrenUri;
		
		try {
			String docId;
			if (DocumentsContract.isDocumentUri(Global.getInstance(), uri)) {
				docId = DocumentsContract.getDocumentId(uri);
			} else {
				docId = DocumentsContract.getTreeDocumentId(uri);
			}
			childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, docId);
		} catch (IllegalArgumentException e) {
			Log.w(Global.APP_TAG, "Invalid URI: " + uri);
			return null;
		}
		
		String[] projection = {
				DocumentsContract.Document.COLUMN_DOCUMENT_ID,
				DocumentsContract.Document.COLUMN_DISPLAY_NAME,
				DocumentsContract.Document.COLUMN_MIME_TYPE
		};
		
		try (Cursor cursor = resolver.query(childrenUri, projection, null, null, null)) {
			if (cursor != null) {
				int idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
				int nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
				int mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
				
				while (cursor.moveToNext()) {
					String name = cursor.getString(nameCol);
					if (name == null || name.startsWith(".")) continue;
					
					String documentId = cursor.getString(idCol);
					String mimeType = cursor.getString(mimeCol);
					Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId);
					
					if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
						Directory new_directory = new Directory(name, fileUri);
						new_directory.setChildren(load_library(fileUri));
						elements.add(new_directory);
					} else {
						int lastDotIndex = name.lastIndexOf('.');
						if (lastDotIndex > 0) {
							String extension = name.substring(lastDotIndex + 1).toLowerCase();
							if (Global.AUDIO_EXTENSIONS.contains(extension)) {
								elements.add(new Music(name, fileUri, extension));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.w(Global.APP_TAG, "Query failed: " + uri, e);
		}
		
		elements.sort(Comparator.comparing(DiskElement::getName));
		return elements;
	}
	
	public static List<DiskElement> load_folder(List<String> path) {
		List<DiskElement> elements = library;
		
		if (path == null ||path.isEmpty()) {
			return elements;
		}
		
		for (String path_element : path) {
			if (path_element == null) {
				continue;
			}
			for (int i = 0; i < Objects.requireNonNull(elements).size(); i ++){
				DiskElement element = elements.get(i);
				if (Objects.equals(element.getName(), path_element) && element instanceof Directory) {
					elements = ((Directory) element).getChildren();
					break;
				}
			}
		}
		return elements;
	}
	
	@Nullable
	public static DiskElement get_element_at_path(List<String> sd_path) {
		if (sd_path == null || sd_path.isEmpty()) {
			return null;
		}
		
		List<String> path = new ArrayList<>(sd_path);
		List<DiskElement> elements = library;
		String last_segment = path.remove(path.size() - 1);
		
		for (String path_element : path) {
			if (path_element == null) {
				continue;
			}
			
			for (int i = 0; i < Objects.requireNonNull(elements).size(); i ++){
				DiskElement element = elements.get(i);
				if (Objects.equals(element.getName(), path_element) && element instanceof Directory) {
					elements = ((Directory) element).getChildren();
					break;
				}
			}
		}
		
		if (elements == null) return null;
		
		for (DiskElement element : elements){
			if (Objects.equals(element.getName(), last_segment)) {
				return element;
			}
		}
		
		return null;
	}
	//endregion
	//region SAF thingy
	public static void create_new_folder(@NonNull List<String> path, String folder_name) {
		Uri treeUri;
		DiskElement parentElement = null;
		
		if (path.isEmpty()) {
			treeUri = DocumentsContract.buildDocumentUriUsingTree(
					library_root,
					DocumentsContract.getTreeDocumentId(library_root)
			);
		} else {
			parentElement = get_element_at_path(path);
			if (parentElement == null) {
				Log.e(Global.APP_TAG, "Parent element not found");
				return;
			}
			treeUri = parentElement.getUri();
		}
		Log.v(Global.APP_TAG, "Creating new folder: " + folder_name + " at " + treeUri.getPath());
		
		ContentResolver resolver = Global.getInstance().getContentResolver();
		try {
			DocumentsContract.createDocument(
					resolver,
					treeUri,
					DocumentsContract.Document.MIME_TYPE_DIR,
					folder_name
			);
			
			Log.v(Global.APP_TAG, "Created new folder: " + folder_name);
			
			Directory newDir = new Directory(folder_name, treeUri);
			if (parentElement == null){
				if (library == null) return;
				library.add(newDir);
			} else {
				((Directory) parentElement).addChild(newDir);
			}
			current_folder = load_folder(Global.getPath());
			Signals.emitSignal("onPathChanged");
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error creating new folder", e);
		}
	}
	//endregion
}
