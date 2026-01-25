package my.little.audio.player.android.ResTree;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.nio.file.*;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
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
			Log.v(Global.APP_TAG, "Confirmed valid directory URI: " + uri.toString());
			
			write_path_config(Global.getInstance(), uri);
			
			ResTree.library_root = uri;
			Global.path = new ArrayList<>();
			
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
	
	public static String getFileName(@NonNull Context context, @NonNull Uri uri) {
		String result = null;
		if (uri.getScheme().equals("content")) {
			try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
				if (cursor != null && cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					if (index != -1) {
						result = cursor.getString(index);
					}
				}
			}
		}
		if (result == null) {
			result = uri.getPath();
			int cut = result.lastIndexOf('/');
			if (cut != -1) {
				result = result.substring(cut + 1);
			}
		}
		return result;
	}
	
	@Nullable
	public static List<DiskElement> load_library(@NonNull Uri uri) {
		List<DiskElement> elements = new ArrayList<>();
		DocumentFile directory = DocumentFile.fromTreeUri(Global.getInstance(), uri);
		
		if (directory == null || !directory.isDirectory()) {
			Log.w(Global.APP_TAG, "Directory not valid: " + uri);
			return null;
		}
		
		DocumentFile[] files = directory.listFiles();
		
		for (DocumentFile file : files) {
			String name = file.getName();
			if (name == null || name.startsWith(".")) continue;
			
			Uri fileUri = file.getUri();
			
			if (file.isDirectory()) {
				Directory new_directory = new Directory(getFileName(Global.getInstance(), fileUri), fileUri);
				new_directory.setChildren(load_library(fileUri));
				elements.add(new_directory);
			} else if (file.isFile()) {
				int lastDotIndex = name.lastIndexOf('.');
				if (lastDotIndex > 0) {
					String extension = name.substring(lastDotIndex + 1).toLowerCase();
					if (Global.AUDIO_EXTENSIONS.contains(extension)) {
						elements.add(new Music(getFileName(Global.getInstance(), fileUri), fileUri, extension));
					}
				}
			}
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
			for (int i = 0; i < elements.size(); i ++){
				DiskElement element = elements.get(i);
				if (Objects.equals(element.getName(), path_element) && element instanceof Directory) {
					elements = ((Directory) element).getChildren();
					break;
				}
			}
		}
		return elements;
	}
}
