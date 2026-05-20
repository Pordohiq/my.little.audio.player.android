package my.little.audio.player.android.queues;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 Licence:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.ResTree.ResTree;

public class Queue {
	protected String name;
	protected List<Music> contents;
	
	protected int position = -1;
	
	//region Con/Destruction
	public Queue(@NonNull String new_name, @NonNull List<Music> new_contents){
		contents = new_contents;
		name = new_name;
	}
	
	public Queue(@NonNull String asString){
		if (!asString.contains(">")) return;
		
		String[] parts = asString.split(">", 2);
		this.name = parts[0];
		
		String paths = parts[1];
		this.contents = new ArrayList<>();
		if (!paths.isEmpty()) {
			String[] local_paths = paths.split(":");
			for (String s : local_paths) {
				if (Objects.equals(s, "")) continue;
				List<String> list = Arrays.asList(s.split("/"));
				Music music = (Music) ResTree.get_element_at_path(list);
				contents.add(music);
			}
		}
	}
	
	@NonNull @Override
	public String toString() {
		StringBuilder result = new StringBuilder(name + ">");
		for (Music song : contents) {
			result.append(":");
			List<String> path = ResTree.get_local_element_path(song, null, null);
			if (path == null) continue;
			boolean first_run = true;
			for (String part : path) {
				if (first_run) {
					first_run = false;
				} else {
					result.append("/");
				}
				result.append(part);
			}
		}
		return result.toString();
	}
	//endregion
	
	public int get_song_count() { return contents.size(); }
	
	//region Audio Playback
	@Nullable
	public Music get_next_song(boolean loop){
		return get_next_song(loop, contents);
	}
	
	@Nullable
	public Music get_previous_song(boolean loop){
		return get_previous_song(loop, contents);
	}
	
	@Nullable
	protected Music get_next_song(boolean loop, @NonNull List<Music> songs){
		if (songs.isEmpty()) return null;
		try {
			position ++;
			if (position  >= get_song_count()){
				if (loop) position = 0;
				else return null;
			}
			return songs.get(position);
		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			Log.e(Global.APP_TAG, "Index out of bounds, when getting queue element from " + name);
		}
		return null;
	}
	
	@Nullable
	protected Music get_previous_song(boolean loop, @NonNull List<Music> songs){
		if (songs.isEmpty()) return null;
		try {
			position --;
			if (position < 0){
				if (loop) position = get_song_count() - 1;
				else return null;
			}
			return songs.get(position);
		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			Log.e(Global.APP_TAG, "Index out of bounds, when getting queue element from " + name);
		}
		return null;
	}
	//endregion
	
	public void add_song(@NonNull Music song) {
		if (!contents.contains(song)) contents.add(song);
	}
	
	public void remove_song(@NonNull Music song) {
		contents.remove(song);
	}
	
	public boolean has_song(Music song){
		return contents.contains(song);
	}
	
	public String get_name() {
		return name;
	}
	
	public List<Music> get_songs() {
		return new ArrayList<>(contents);
	}
	
	public void move_song_up(Music song){
		if (!contents.contains(song)) return;
		int index = contents.indexOf(song);
		if (index == 0) return;
		Music tmp = contents.get(index - 1);
		contents.set(index - 1, contents.get(index));
		contents.set(index, tmp);
	}
	
	public void move_song_down(Music song){
		if (!contents.contains(song)) return;
		int index = contents.indexOf(song);
		if (index == contents.size() - 1) return;
		Music tmp = contents.get(index + 1);
		contents.set(index + 1, contents.get(index));
		contents.set(index, tmp);
	}
	
	public void set_name(@NonNull String new_name) {
		name = new_name;
	}
}
