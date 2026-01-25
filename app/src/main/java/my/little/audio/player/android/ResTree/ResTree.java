package my.little.audio.player.android.ResTree;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.nio.file.*;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import my.little.audio.player.android.Global;

public class ResTree {
	public static String library_root;
	@Nullable
	public static List<DiskElement> library;
	public static List<DiskElement> current_folder;
	
	public static void init(Context context) {
		library_root = read_path_config(context);
		Log.i(Global.APP_TAG, "Library root: " + library_root);
		library = load_library(library_root);
		current_folder = library;
	}
	
	public static void reload_from_disk(Context context) {
		Log.v(Global.APP_TAG, "Reloading library path from disk");
		library_root = read_path_config(context);
		Log.v(Global.APP_TAG, "Reloading library tree from disk");
		library = null;
		library = load_library(library_root);
		current_folder = library;
	}
	
	public static void set_library_path(String path) {
		Log.w(Global.APP_TAG, "Setting library path: " + path);
		if (path != null && Files.exists(Paths.get(path))){
			Log.v(Global.APP_TAG, "Setting library path: " + path);
			write_path_config(Global.getInstance(), path);
			library_root = path;
			Global.path = new ArrayList<>();
			library = load_library(library_root);
			current_folder = library;
		}
		else {
			Log.w(Global.APP_TAG, "Invalid path: " + path);
		}
	}
	
	@NonNull
	private static String read_path_config(@NonNull Context context) {
		String standard_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
		File filesDir = context.getFilesDir();
		if (filesDir == null) {
			write_path_config(context, standard_path);
			Log.e(Global.APP_TAG, "Error reading path config, default path: " + standard_path);
			return standard_path;
		}
		
		Path pathConfig = filesDir.toPath().resolve("library.path");
		String content;
		
		try {
			if (Files.exists(pathConfig)) {
				byte[] bytes = Files.readAllBytes(pathConfig);
				content = new String(bytes, StandardCharsets.UTF_8).trim();
			} else {
				content = "";
			}
		} catch (IOException e) {
			write_path_config(context, standard_path);
			Log.e(Global.APP_TAG, "Error reading path config, default path: " + standard_path);
			return standard_path;
		}
		
		if (Files.exists(Paths.get(content)) && !content.isEmpty()) {
			return content;
		} else {
			write_path_config(context, standard_path);
			Log.e(Global.APP_TAG, "Error reading path config, default path: " + standard_path);
			return standard_path;
		}
	}
	
	private static void write_path_config(@NonNull Context context, String newPath) {
		Log.v(Global.APP_TAG, "Writing path config: " + newPath);
		File filesDir = context.getFilesDir();
		if (filesDir == null) return;
		
		Path pathConfig = filesDir.toPath().resolve("library.path");
		
		try {
			Files.write(pathConfig, newPath.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error writing path config, new path: " + newPath);
		}
	}
	
	@Nullable
	public static List<DiskElement> load_library(@NonNull String path) {
		Path startPath = Paths.get(path);
		List<DiskElement> elements = new ArrayList<>();
		
		if (!Files.isDirectory(startPath)) {
			Log.w(Global.APP_TAG, "Directory not valid: " + path);
			return null;
		}
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(startPath)) {
			for (Path entry : stream) {
				String name = entry.getFileName().toString();
				String absolutePath = entry.toAbsolutePath().toString();
				
				if (Files.isDirectory(entry) && !name.startsWith(".")) {
					Directory new_directory = new Directory(absolutePath);
					new_directory.setChildren(load_library(absolutePath));
					elements.add(new_directory);
				} else if (Files.isRegularFile(entry)) {
					int lastDotIndex = name.lastIndexOf('.');
					
					if (lastDotIndex > 0) {
						String extension = name.substring(lastDotIndex + 1).toLowerCase();
						if (Global.AUDIO_EXTENSIONS.contains(extension)) {
							elements.add(new Music(absolutePath));
						}
					}
				} else {
					Log.d(Global.APP_TAG, "File not valid: " + absolutePath);
				}
			}
		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error reading directory: " + path, e);
			return null;
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
