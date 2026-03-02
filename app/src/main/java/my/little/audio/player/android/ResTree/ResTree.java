package my.little.audio.player.android.ResTree;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.nio.file.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.documentfile.provider.DocumentFile;

import my.little.audio.player.android.Global;

public class ResTree {
	public static Uri library_root;
	@Nullable
	public static List<DiskElement> library;
	public static List<DiskElement> current_folder;
	
	//region Path config
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
			} catch (IllegalArgumentException argumentException) {
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
	//endregion
	//region ResTree Interactions
	@Nullable
	private static List<DiskElement> load_library(@NonNull Uri uri) {
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
		
		elements.sort((o1, o2) -> {
			String name1 = (o1 instanceof Directory ? "0" : "1") + o1.getName().toLowerCase();
			String name2 = (o2 instanceof Directory ? "0" : "1") + o2.getName().toLowerCase();
			return name1.compareTo(name2);
		});
		return elements;
	}
	
	@Nullable
	public static List<DiskElement> load_folder(List<String> path) {
		if (library == null) return null;
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
		
		if (elements == null) return null;
		
		elements.sort((o1, o2) -> {
			String name1 = (o1 instanceof Directory ? "0" : "1") + o1.getName().toLowerCase();
			String name2 = (o2 instanceof Directory ? "0" : "1") + o2.getName().toLowerCase();
			return name1.compareTo(name2);
		});
		return elements;
	}
	
	@Nullable
	public static DiskElement get_element_at_path(@NonNull List<String> sd_path) {
		if (sd_path.isEmpty()) {
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

	@Nullable
	public static List<String> get_local_element_path(@NonNull DiskElement element, @Nullable List<DiskElement> data, @Nullable List<String> path) {
		if (library == null) return null;
		if (data == null) data = library;
		if (path == null) path = new ArrayList<>();

		for (DiskElement disk_element : data) {
			if (disk_element == element){ // If the child is found
				path.add(disk_element.getName());
				return path;
			}

			if (disk_element instanceof Directory) { // If the child is Directory, recurse.
				if (((Directory) disk_element).getChildCount() <= 0) continue; // Check that the dir has children, else skip.
				List<String> new_path = new ArrayList<>(path);
				new_path.add(disk_element.getName());
				List<String> result = get_local_element_path(element, ((Directory) disk_element).getChildren(), new_path);
				if (result == null) continue;
				return result;
			}
		}
        return null; // If we have found nothing, return null.
    }

	@Nullable
	private static Directory get_parent(DiskElement element){
		if (element == null || library == null) return null;
		List<String> childPath = get_local_element_path(element, library, new ArrayList<>());
		if (childPath == null) return null;
		childPath.remove(childPath.size() - 1);
		if (childPath.isEmpty() || get_element_at_path(childPath) == null) { return null; }
        return (Directory) get_element_at_path(childPath);
	}
	
	@Nullable
	public static DiskElement get_element_by_Uri(@NonNull Uri needed_uri, @Nullable List<DiskElement> data){
		if (library == null) return null;
		if (data == null) data = library;
		for (DiskElement element : data){
			if (element.getUri() == needed_uri){
				return element;
			}
			if (element instanceof Directory){
				return get_element_by_Uri(needed_uri, ((Directory) element).getChildren());
			}
		}
		return null;
	}
	
	@NonNull
	public static List<Music> get_musics_at_path(@NonNull List<String> path, boolean recursive, @Nullable List<DiskElement> data) {
		List<Music> result = new ArrayList<>();
		List<DiskElement> elements = load_folder(path);
		if (elements == null) return new ArrayList<>();
		
		for (DiskElement element : elements) {
			if (element instanceof Music){
				result.add((Music) element);
			} else if (recursive && element instanceof Directory) {
				List<String> subPath = new ArrayList<>(path);
				subPath.add(element.getName());
				result.addAll(get_musics_at_path(subPath, true, data));
			}
		}
		return result;
	}
	
	//endregion
	//region SAF Interactions
	@Nullable
	private static Uri SAF_create_folder(Uri parentUri, String folderName) {
		ContentResolver resolver = Global.getInstance().getContentResolver();
		try {
			return DocumentsContract.createDocument(
					resolver,
					parentUri,
					DocumentsContract.Document.MIME_TYPE_DIR,
					folderName
			);
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error creating SAF directory", e);
			return null;
		}
	}

	private static void SAF_delete_element(@NonNull DocumentFile file) {
		if (file.isDirectory()) {
			for (DocumentFile child : file.listFiles()) {
				SAF_delete_element(child);
			}
		}
		file.delete();
	}

	private static void SAF_delete_element(@NonNull Uri uri) throws IOException {
		DocumentFile documentFile = get_DocumentFile_from_Uri(uri);

		if (documentFile == null) {
			throw new IOException("Source Element not valid");
		}

		SAF_delete_element(documentFile);
	}

	@Nullable
	private static Uri SAF_copy_element(@NonNull Context context, @NonNull DocumentFile source, @NonNull DocumentFile targetParent) throws IOException {
		if (source.isDirectory()) {
			DocumentFile newDir = targetParent.createDirectory(Objects.requireNonNull(source.getName()));
			if (newDir == null) return null;

			for (DocumentFile child : source.listFiles()) {
				SAF_copy_element(context, child, newDir);
			}
			return newDir.getUri();
		} else {
			DocumentFile newFile = targetParent.createFile(Objects.requireNonNull(source.getType()), Objects.requireNonNull(source.getName()));
			if (newFile == null) return null;

			try (InputStream in = context.getContentResolver().openInputStream(source.getUri());
				 OutputStream out = context.getContentResolver().openOutputStream(newFile.getUri())) {
				byte[] buf = new byte[8192];
				int len;
				while (in != null && out != null && (len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
				}
			}
			return newFile.getUri();
		}
	}

	private static Uri SAF_copy_element(@NonNull Uri sourceUri, @NonNull Uri targetParentUri) throws IOException {
		Context context = Global.getInstance();

		DocumentFile sourceFile = get_DocumentFile_from_Uri(sourceUri);
		DocumentFile targetDir = get_DocumentFile_from_Uri(targetParentUri);

		if (sourceFile == null || targetDir == null) {
			throw new IOException("Source or Target Dir not valid");
		}

		return SAF_copy_element(context, sourceFile, targetDir);
	}

	@Nullable
	private static Uri SAF_rename_element(@NonNull DocumentFile file, @NonNull String newName) {
		if (file.exists()) {
			String displayName = file.getName();
			String extension = "";

			if (displayName != null) {
				int lastDot = displayName.lastIndexOf(".");
				if (lastDot != -1) {
					extension = displayName.substring(lastDot);
				}
			}

			String finalName = newName;
			if (!extension.isEmpty() && !newName.toLowerCase().endsWith(extension.toLowerCase())) {
				finalName = newName + extension;
			}

			if (file.renameTo(finalName)) {
				return file.getUri();
			}
		}
		return null;
	}

	@Nullable
	private	static Uri SAF_rename_element(@NonNull Uri uri, @NonNull String newName) {
		DocumentFile file = get_DocumentFile_from_Uri(uri);
		if (file == null) return null;
		return SAF_rename_element(file, newName);
	}

	@Nullable
    private static DocumentFile get_DocumentFile_from_Uri(@NonNull Uri uri) {
		DocumentFile file;
		if (DocumentsContract.isTreeUri(uri)) {
			file = DocumentFile.fromTreeUri(Global.getInstance(), uri);
		} else {
			file = DocumentFile.fromSingleUri(Global.getInstance(), uri);
		}
		return file;
	}

	@Nullable
    private static String get_fileName_from_Uri(@NonNull Uri uri) {
		return Objects.requireNonNull(get_DocumentFile_from_Uri(uri)).getName();
	}
	//endregion
	//region API
	public static void init(Context context) {
		library_root = read_path_config(context);
		if (library_root != null) {
			Log.i(Global.APP_TAG, "Library root: " + library_root);
			library = load_library(library_root);
			current_folder = library;
		}
	}
	
	public static boolean is_subPath(@NonNull List<String> list, @NonNull List<String> prefix) {
		if (prefix.size() > list.size()) {
			return false;
		}
		return list.subList(0, prefix.size()).equals(prefix);
	}

	public static void add_audio_file(@NonNull Uri audio_uri, @NonNull List<String> path){
		Uri destUri;
		if (path.isEmpty()) {
			destUri = library_root;
		} else {
			Directory dir = (Directory) get_element_at_path(path);
			if (dir == null) {
				destUri = library_root;
			} else {
				destUri = dir.getUri();
			}
		}

		try {
			Uri new_uri = SAF_copy_element(audio_uri, destUri);
			String new_name = get_fileName_from_Uri(new_uri);
			assert new_name != null;

			Music file = new Music(
					new_name,
					audio_uri,
					new_name.split("\\.")[1]
			);

			if (path.isEmpty()) {
				assert library != null;
				library.add(file);
			} else {
				Directory dir = (Directory) get_element_at_path(path);
				assert dir != null;
				dir.addChild(file);
			}

		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error adding audio file to library root ", e);
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

	public static void create_new_folder(@NonNull List<String> path, String folder_name) {
		Uri parentUri;
		DiskElement parentElement = null;

		if (path.isEmpty()) {
			parentUri = DocumentsContract.buildDocumentUriUsingTree(
					library_root,
					DocumentsContract.getTreeDocumentId(library_root)
			);
		} else {
			parentElement = get_element_at_path(path);
			if (parentElement == null) return;
			parentUri = parentElement.getUri();
		}

		Uri newFolderUri = SAF_create_folder(parentUri, folder_name);

		if (newFolderUri != null) {
			Directory newDir = new Directory(folder_name, newFolderUri);

			if (parentElement == null) {
				if (library != null) library.add(newDir);
			} else {
				((Directory) parentElement).addChild(newDir);
			}

			current_folder = load_folder(Global.getPath());
		}
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

	public static void move_file(@NonNull DiskElement element, @NonNull List<String> path) {
		DiskElement currentParent = get_parent(element);
		DiskElement targetParent = get_element_at_path(path);

		Uri parentUri = targetParent == null ? library_root : targetParent.getUri();

		if (currentParent == targetParent) {
			Log.w(Global.APP_TAG, "Element already in target path");
			return;
		}

		try {
			Uri newUri = SAF_copy_element(element.getUri(), parentUri);

			if (newUri == null) {
				Log.e(Global.APP_TAG, "Couldn't copy file before moving");
				return;
			}

			delete_file(element);

			element.move(get_fileName_from_Uri(newUri), newUri);

			if (targetParent instanceof Directory) {
				((Directory) targetParent).addChild(element);
			} else {
				assert library != null;
				library.add(element);
			}
		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error moving element", e);
		}
	}

	public static void rename_file(DiskElement element, String newName) {
		if (element == null) {
			Log.e(Global.APP_TAG, "Element not found");
			return;
		}
		try {
			Uri newUri = SAF_rename_element(element.getUri(), newName);
			if (newUri == null) throw new Exception("The new Uri is not valid");
			element.move(get_fileName_from_Uri(newUri), newUri);
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error renaming file", e);
        }
	}

	public static void delete_file(DiskElement element) {
		if (element == null) {
			Log.e(Global.APP_TAG, "Element not found");
			return;
		}
		Uri uri = element.getUri();

		try {
			SAF_delete_element(uri);
		} catch (FileNotFoundException e) {
			Log.e(Global.APP_TAG, "File not found, could not delete: " + element.getName());
			return;
		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error deleting file: " + element.getName());
			return;
		}

		Directory parent = get_parent(element);
		if (parent == null) {
			if (library == null) return;
			library.remove(element);
		} else {
			parent.removeChild(element);
		}
	}
	//endregion
}
