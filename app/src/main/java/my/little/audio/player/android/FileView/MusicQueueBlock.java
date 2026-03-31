package my.little.audio.player.android.FileView;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.queues.Queue;
import my.little.audio.player.android.queues.Queues;

public class MusicQueueBlock extends LinearLayout {
	private Music song;
	private Queue queue;
	
	private TextView name;
	
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
		
		name = findViewById(R.id.music_c_name);
	}
	
	public void setUp(@NonNull Music music, Queue queue){
		song = music;
		this.queue = queue;
		name.setText(music.getName());
	}
}
