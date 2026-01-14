package my.little.audio.player.java.ResTree;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.Objects;

import android.util.Log;

import my.little.audio.player.java.Global;

public class ResTree {
	public static String library_root;
	public static List<DiskElement> library;
	public static List<DiskElement> current_folder;
	
	public static void init(String new_library_root) {
		library_root = new_library_root;
		library = load_library(library_root);
		current_folder = library;
	}
	
	public static List<DiskElement> load_library(String path){
		File directory = new File(path);
		List<DiskElement> elements = new ArrayList<>();
		
		if (!directory.isDirectory()) {
			Log.w(Global.APP_TAG, "Directory not valid: " + path);
			return null;
		}
		
		File[] contents = directory.listFiles();
		
		if (contents == null){
			Log.w(Global.APP_TAG, "Directory not valid: " + path);
			return null;
		}
		
		for (File file : contents) {
			if (file.isDirectory() && !file.getName().startsWith(".")) {
				Directory new_directory = new Directory(file.getAbsolutePath());
				new_directory.setChildren(load_library(file.getAbsolutePath()));
				elements.add(new_directory);
			} else if (file.isFile()) {
				String fileName = file.getName();
				int lastDotIndex = fileName.lastIndexOf('.');
				
				if (lastDotIndex > 0) {
					String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
					if (Global.AUDIO_EXTENSIONS.contains(extension)) {
						elements.add(new Music(file.getAbsolutePath()));
					}
				}
			} else {
				Log.d(Global.APP_TAG, "File not valid: " + file.getAbsolutePath());
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
