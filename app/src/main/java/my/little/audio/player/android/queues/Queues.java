package my.little.audio.player.android.queues;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.Signals;

public class Queues {
	@NonNull
	private static final List<Queue> loaded_queues = new ArrayList<>();
	@Nullable
	private static Queue active_queue;
	
	private static void save_queues(){
		StringBuilder queues_as_string = new StringBuilder();
		for (Queue queue: loaded_queues) {
			queues_as_string.append(queue.toString()).append("\n");
		}
		Log.v(Global.APP_TAG, "Writing queues: " + queues_as_string);
		File filesDir = Global.getInstance().getFilesDir();
		if (filesDir == null) return;
		
		Path pathConfig = filesDir.toPath().resolve("queues.list");
		
		try {
			Files.write(pathConfig, queues_as_string.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error writing path config, new path: " + queues_as_string);
		}
	}
	
	private static void read_queues() {
		File filesDir = Global.getInstance().getFilesDir();
		
		if (filesDir == null) return;
		
		File pathConfigFile = new File(filesDir, "queues.list");
		String content = "";
		
		try {
			if (pathConfigFile.exists()) {
				byte[] bytes = Files.readAllBytes(pathConfigFile.toPath());
				content = new String(bytes, StandardCharsets.UTF_8).trim();
			}
		} catch (IOException e) {
			Log.e(Global.APP_TAG, "Error reading queue save file.");
			return;
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "There was exception, when trying to read queue save file.");
		}
		
		if (!content.isEmpty()) {
			String[] lines = content.split("\n");
			for (String line : lines) {
				loaded_queues.add(new Queue(line));
			}
		}
	}
	
	public static void init(){
		read_queues();
		Signals.createEvent("onQueueLibChanged");
	}
	
	public static void create_new_queue(@NonNull String name){
		loaded_queues.add(new Queue(name, new ArrayList<>()));
		save_queues();
		Signals.emitSignal("onQueueLibChanged");
	}
	
	@NonNull
	public static List<Queue> get_queues (){
		return new ArrayList<>(loaded_queues);
	}
	
	public static void set_active_queue(@NonNull Queue queue){
		if (!loaded_queues.contains(queue)) return;
		active_queue = queue;
	}
	
	@Nullable
	public static Queue get_active_queue(){
		return active_queue;
	}
}
