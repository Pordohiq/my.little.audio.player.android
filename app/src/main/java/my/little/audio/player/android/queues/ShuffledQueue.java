package my.little.audio.player.android.queues;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 Licence:
// https://github.com/Pordohiq/my.little.audio.player.android

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import my.little.audio.player.android.ResTree.Music;

public class ShuffledQueue extends Queue {
	private final Queue derivative;
	private int shuffle_seed;
	private List<Music> shuffled_songs;
	
	public ShuffledQueue(@NonNull Queue derivative){
		super(derivative.get_name(), derivative.get_songs());
		shuffle_seed = (int)(Math.random() * (100));
		this.derivative = derivative;
		re_shuffle(true);
	}
	
	private void re_shuffle(boolean direction){
		if(direction) shuffle_seed ++;
		else shuffle_seed --;
		
		shuffled_songs = get_songs();
		Collections.shuffle(shuffled_songs, new Random(shuffle_seed));
	}
	
	@Nullable @Override
	public Music get_next_song(boolean loop) {
		if (position >= get_song_count()) position = -1;
		Music next_song = super.get_next_song(false, shuffled_songs);
		if (next_song == null && loop){
			re_shuffle(true);
			super.position = -1;
			next_song = super.get_next_song(false, shuffled_songs);
		}
		return next_song;
	}
	
	@Nullable @Override
	public Music get_previous_song(boolean loop) {
		if (position == -1) position = shuffled_songs.toArray().length;
		Music next_song = super.get_previous_song(false, shuffled_songs);
		if (next_song == null && loop){
			re_shuffle(false);
			super.position = shuffled_songs.toArray().length;
			next_song = super.get_previous_song(false, shuffled_songs);
		}
		return next_song;
	}
	
	@Override
	public void add_song(@NonNull Music song) {
		super.add_song(song);
		if (!shuffled_songs.contains(song)) shuffled_songs.add(song);
		if (!derivative.contents.contains(song)) derivative.contents.add(song);
	}
	
	@Override
	public void remove_song(@NonNull Music song) {
		super.remove_song(song);
		shuffled_songs.remove(song);
		derivative.contents.remove(song);
	}
	
	public Queue upcast(){
		if (shuffled_songs.size() <= position || position < 0){
			position = 0;
		}
		int position_in_queue = derivative.get_songs().indexOf(shuffled_songs.get(position));
		if (position_in_queue < 0){
			position_in_queue = 0;
		}
		derivative.position = position_in_queue;
		return derivative;
	}
	
	public void rotate_queue_to_song(@NonNull Music song){
		re_shuffle(true);
		int song_position = shuffled_songs.indexOf(song);
		if (song_position != -1) {
			Collections.rotate(shuffled_songs, -song_position);
		}
		
		position = 0;
	}
}
