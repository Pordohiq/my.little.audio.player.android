package my.little.audio.player.android.queues;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 Licence:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.MixingState;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.Signals;

public class Queues {
	@NonNull
	public static final List<Queue> loaded_queues = new ArrayList<>();
	@Nullable
	private static Queue active_queue;
	
	//region Loading/Saving
	public static void save_queues(){
		StringBuilder queues_as_string = new StringBuilder();
		for (Queue queue: loaded_queues) {
			queues_as_string.append(queue.toString()).append("\n");
		}
		Log.v(Global.APP_TAG, "Writing queues: " + queues_as_string);
		File filesDir = Global.getInstance().getFilesDir();
		if (filesDir == null) return;
		
		if (Build.VERSION.SDK_INT >= 26) {
			Path pathConfig = filesDir.toPath().resolve("queues.list");
			
			try {
				Files.write(pathConfig, queues_as_string.toString().getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				Log.e(Global.APP_TAG, "Error writing path config, new path: " + queues_as_string);
			}
		} else {
			File pathConfigFile = new File(filesDir, "queues.list");
			
			try{
				if (!pathConfigFile.exists()) {
					boolean success = pathConfigFile.createNewFile();
					if (!success) throw new IOException();
				}
				
				try (FileOutputStream fos = new FileOutputStream(pathConfigFile)) {
					fos.write(queues_as_string.toString().getBytes(StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				Log.e(Global.APP_TAG, "Error writing path config in legacy Queues, new path: " + queues_as_string);
			}
		}
	}
	
	private static void read_queues() {
		File filesDir = Global.getInstance().getFilesDir();
		
		if (filesDir == null) return;
		
		File pathConfigFile = new File(filesDir, "queues.list");
		String content = "";
		
		if (Build.VERSION.SDK_INT >= 26) {
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
		} else {
			try (FileInputStream fis = new FileInputStream(pathConfigFile)) {
				if (pathConfigFile.exists()) {
					byte[] bytes = new byte[(int) pathConfigFile.length()];
					int char_read = fis.read(bytes);
					if (char_read <= 0) throw new IOException();
					content = new String(bytes, StandardCharsets.UTF_8).trim();
				}
			} catch (IOException e) {
				Log.e(Global.APP_TAG, "Error reading queue save file in legacy Queues.");
				return;
			}
		}
		
		if (!content.isEmpty()) {
			String[] lines = content.split("\n");
			for (String line : lines) {
				Log.v(Global.APP_TAG, "Loading queue: " + line);
				loaded_queues.add(new Queue(line));
			}
		}
	}
	
	public static void print_queues(){
		for (Queue queue : loaded_queues) {
			Log.v(Global.APP_TAG, queue.toString());
		}
		if (active_queue != null) Log.v(Global.APP_TAG, "Active queue: " + active_queue);
	}
	
	private static void cleanup_queues(){
		if (ResTree.library == null) {
			Log.w(Global.APP_TAG, "Library not loaded, skipping cleanup");
			return;
		}
		for (Queue queue : loaded_queues) {
			for (Music music : queue.get_songs()) {
				if (ResTree.get_local_element_path(music, null, null) == null){
					queue.remove_song(music);
				}
			}
		}
	}
	//endregion
	
	public static void init(){
		while (!loaded_queues.isEmpty()){
			loaded_queues.remove(0);
		}
		
		read_queues();
		print_queues();
		cleanup_queues();
		save_queues();
		Signals.createEvent("onQueueLibChanged");
		Signals.createEvent("onQueueSet");
		
		Signals.subscribeToEvent("onMxStateChanged", Queues::on_mx_state_changed);
		Signals.subscribeToEvent("onPathChanged", Queues::on_path_changed);
	}
	
	//region Signals
	private static void on_path_changed(){
		if (active_queue == null) {
			return;
		}
		if (active_queue instanceof FolderQueue) {
			if (((FolderQueue) active_queue).is_recursive()){
				List<String> glob_path = Global.getPath();
				List<String> rfq_path = ((FolderQueue) active_queue).get_path();
				
				if (!ResTree.is_subPath(glob_path, rfq_path)) {
					Global.mx_state.deactivate_queue();
				}
			}
			else{
				Global.mx_state.deactivate_queue();
			}
		} else if (active_queue instanceof ShuffledQueue && ((ShuffledQueue) active_queue).upcast() instanceof FolderQueue) {
			FolderQueue fq = (FolderQueue) ((ShuffledQueue) active_queue).upcast();
			if (fq.is_recursive()) {
				if(!ResTree.is_subPath(Global.getPath(), fq.get_path())){
					Global.mx_state.deactivate_queue();
				}
			} else {
				Global.mx_state.deactivate_queue();
			}
		}
	}
	
	private static void on_mx_state_changed(){
		Log.i(Global.APP_TAG, Global.mx_state.get_queue_state().toString());
		
		if (Global.mx_state.get_queue_state() == MixingState.queue_state.NONE) {
			unset_active_queue();
			Signals.emitSignal("onQueueSet");
			return;
		}
		
		if (Global.mx_state.get_queue_state() == MixingState.queue_state.LOADED_QUEUE){
			if (active_queue == null){
				Global.mx_state.deactivate_queue();
			}
			else if (active_queue instanceof ShuffledQueue){
				active_queue = ((ShuffledQueue) active_queue).upcast();
			}
		} else if (Global.mx_state.get_queue_state() == MixingState.queue_state.DIRECTORY) {
			if (active_queue instanceof ShuffledQueue && ((ShuffledQueue) active_queue).upcast() instanceof FolderQueue){
				active_queue = ((ShuffledQueue) active_queue).upcast();
			} else {
				active_queue = new FolderQueue(Global.getPath(), false);
				if (Global.current_audio != null) move_pointer_to_song(Global.current_audio);
				Signals.emitSignal("onQueueSet");
			}
		} else if (Global.mx_state.get_queue_state() == MixingState.queue_state.RECURSIVE_DIRECTORY) {
			if (active_queue instanceof ShuffledQueue && ((ShuffledQueue) active_queue).upcast() instanceof FolderQueue){
				active_queue = ((ShuffledQueue) active_queue).upcast();
			} else {
				active_queue = new FolderQueue(Global.getPath(), true);
				if (Global.current_audio != null) move_pointer_to_song(Global.current_audio);
				Signals.emitSignal("onQueueSet");
			}
		}
		
		if (Global.mx_state.get_shuffle_state() == MixingState.shuffle_state.ON) {
			if (active_queue == null) return;
			active_queue = new ShuffledQueue(active_queue);
		}
	}
	//endregion
	//region QueueInteractions
	public static boolean queue_exists(@NonNull String queue_name){
		for (Queue queue : loaded_queues){
			if (Objects.equals(queue.get_name(), queue_name)){
				return true;
			}
		}
		return false;
	}
	
	public static void create_new_queue(@NonNull String name){
		if (queue_exists(name)){
			int appendix = 1;
			name += "_1";
			while (queue_exists(name)) {
				appendix++;
				int idx = name.lastIndexOf('_');
				if (idx != -1) {
					name = name.substring(0, idx + 1) + appendix;
				}
			}
		}
		
		loaded_queues.add(new Queue(name, new ArrayList<>()));
		save_queues();
		Signals.emitSignal("onQueueLibChanged");
	}
	
	public static void rename_queue(@NonNull Queue queue, @NonNull String new_name){
		if (!loaded_queues.contains(queue)) return;
		
		if (queue_exists(new_name)){
			int appendix = 1;
			new_name += "_1";
			while (queue_exists(new_name)) {
				appendix++;
				int idx = new_name.lastIndexOf('_');
				if (idx != -1) {
					new_name = new_name.substring(0, idx + 1) + appendix;
				}
			}
		}
		
		queue.set_name(new_name);
		save_queues();
		Signals.emitSignal("onQueueLibChanged");
	}
	
	public static void add_music_to_queue(@NonNull Music music, @NonNull Queue queue){
		if (queue.has_song(music)) {
			queue.remove_song(music);
		} else {
			queue.add_song(music);
		}
		Log.v(Global.APP_TAG, queue.toString());
		save_queues();
		Signals.emitSignal("onQueueLibChanged");
	}
	//endregion
	//region API
	@NonNull
	public static List<Queue> get_queues (){
		return new ArrayList<>(loaded_queues);
	}
	
	public static void set_active_queue(@NonNull Queue queue){
		if (!loaded_queues.contains(queue)) return;
		if (queue == active_queue) unset_active_queue();
		active_queue = queue;
		Global.mx_state.activate_loaded_queue();
		if (Global.current_audio != null) move_pointer_to_song(Global.current_audio);
		Signals.emitSignal("onQueueSet");
	}
	
	public static void unset_active_queue(){
		active_queue = null;
		if (Global.mx_state.get_queue_state() == MixingState.queue_state.LOADED_QUEUE) Global.mx_state.toggle_queue_state();
	}
	
	@Nullable
	public static Queue get_active_queue(){
		if (active_queue == null) return null;
		if (active_queue instanceof ShuffledQueue){
			ShuffledQueue sq = (ShuffledQueue) active_queue;
			return sq.upcast();
		}
		return active_queue;
	}
	
	@Nullable
	public static Music get_next_song_from_active_queue(boolean loop){
		if (active_queue == null) return null;
		return active_queue.get_next_song(loop);
	}
	
	@Nullable
	public static Music get_prev_song_from_active_queue(boolean loop){
		if (active_queue == null) return null;
		return active_queue.get_previous_song(loop);
	}
	
	public static void move_pointer_to_song(@NonNull Music song){
		if (active_queue == null) return;
		if (!active_queue.has_song(song)) return;
		if (active_queue instanceof ShuffledQueue){
			((ShuffledQueue) active_queue).rotate_queue_to_song(song);
		} else {
			active_queue.position = active_queue.get_songs().indexOf(song);
		}
	}
	//endregion
}
