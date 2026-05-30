package my.little.audio.player.android.FileView;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 Licence:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.queues.Queue;
import my.little.audio.player.android.queues.Queues;

public class MusicQueueBlock extends LinearLayout {
	private Music song;
	private Queue queue;
	
	private TextView name;
	private ImageView main_icon;
	
	public MusicQueueBlock(Context context) {
		super(context);
		init(context);
	}
	
	public MusicQueueBlock(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.music_queue_content_block, this, true);
		
		ImageView up_button = findViewById(R.id.up_icon);
		ImageView down_button = findViewById(R.id.down_icon);
		
		up_button.setOnClickListener(view -> {
			if (queue == null || song == null) return;
			queue.move_song_up(song);
			Signals.emitSignal("onQueueLibChanged");
			Queues.save_queues();
		});
		down_button.setOnClickListener(view -> {
			if (queue == null || song == null) return;
			queue.move_song_down(song);
			Signals.emitSignal("onQueueLibChanged");
			Queues.save_queues();
		});
		
		findViewById(R.id.music_c_name).setOnClickListener(view -> {
			Queues.move_pointer_to_song(song);
			Global.setAudio(song, true);
		});
		findViewById(R.id.music_c_icon).setOnClickListener(view -> {
			Queues.move_pointer_to_song(song);
			Global.setAudio(song, true);
		});
		
		name = findViewById(R.id.music_c_name);
		main_icon = findViewById(R.id.music_c_icon);
		Signals.subscribeToEvent("onAudioSet", this::check_main_icon);
		Signals.subscribeToEvent("onSongFinished", this::check_main_icon);
		check_main_icon();
	}
	
	public void setUp(@NonNull Music music, Queue queue){
		song = music;
		this.queue = queue;
		name.setText(music.getName());
		check_main_icon();
	}
	
	private void check_main_icon(){
		if (song == Global.current_audio && Global.current_audio != null) {
			main_icon.setImageResource(R.drawable.block_music_active);
		} else {
			main_icon.setImageResource(R.drawable.block_music_queue);
		}
	}
}
