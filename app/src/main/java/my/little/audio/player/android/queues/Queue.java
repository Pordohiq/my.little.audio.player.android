package my.little.audio.player.android.queues;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import my.little.audio.player.android.Global;

public class Queue {
	protected String name;
	protected List<Uri> contents;
	
	private int position = -1;
	
	public Queue(@NonNull String new_name, @NonNull List<Uri> new_contents){
		contents = new_contents;
		name = new_name;
	}
	
	public Queue(@NonNull String asString){
		if (!asString.contains(">")) return;
		
		String[] parts = asString.split(">", 2);
		this.name = parts[0];
		
		this.contents = new ArrayList<>();
		if (parts.length > 1 && !parts[1].isEmpty()) {
			String[] uriStrings = parts[1].split(",");
			for (String s : uriStrings) {
				this.contents.add(Uri.parse(s));
			}
		}
	}
	
	public int get_song_count() { return contents.size(); }
	
	@Nullable
	public Uri get_next_song(boolean loop){
		try {
			position ++;
			
		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			Log.e(Global.APP_TAG, "Index out of bounds, when getting queue element from " + name);
		}
		return null;
	}
	
	@NonNull @Override
	public String toString() {
		StringBuilder result = new StringBuilder(name + ">");
		for (Uri uri : contents) {
			result.append(uri.toString());
		}
		return result.toString();
	}
	
	public void add_song(@NonNull Uri song) {
		contents.add(song);
	}
	
	public void remove_song(@NonNull Uri song) {
		contents.remove(song);
	}
	
	
	public String getName() {
		return name;
	}
}
