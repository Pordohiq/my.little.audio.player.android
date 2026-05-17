package my.little.audio.player.android.FileView;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.queues.Queues;

public class MusicBlock extends LinearLayout {
	private LinearLayout block;
	private ImageView mainIcon;
	private TextView musicName;
	private ImageView moreIcon;
	
	private Music music;
	
	public MusicBlock(Context context) {
		super(context);
		init(context);
	}
	
	public MusicBlock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.music_block_layout, this, true);
		block = findViewById(R.id.music_block);
		
		mainIcon = findViewById(R.id.music_icon);
		musicName = findViewById(R.id.music_name);
		moreIcon = findViewById(R.id.more_icon);
	}
	
	private void check_more_icon() {
		if (music == Action.get_element() && music != null) {
			moreIcon.setImageResource(R.drawable.block_more_active);
		} else {
			moreIcon.setImageResource(R.drawable.block_more);
		}
	}
	
	private void check_main_icon() {
		if (music == null) return;
		if (Global.invalid_files.contains(music)) {
			mainIcon.setImageResource(R.drawable.block_music_invalid);
		} else if (music == Global.current_audio) {
			mainIcon.setImageResource(R.drawable.block_music_active);
		} else if (Queues.get_active_queue() != null && Queues.get_active_queue().has_song(music)){
			mainIcon.setImageResource(R.drawable.block_music_queue);
		} else {
			mainIcon.setImageResource(R.drawable.block_music);
		}
	}
	
	public void setUp(Music mus) {
		music = mus;
		musicName.setText(music.getName());
		check_main_icon();
		// Hook up buttons
		mainIcon.setOnClickListener(view -> mainClick());
		musicName.setOnClickListener(view -> mainClick());

		moreIcon.setOnClickListener(view -> secondClick());
		
		Signals.subscribeToEvent("onActionElementChanged", this::check_more_icon);
		check_more_icon();
		
		Signals.subscribeToEvent("onLockStateChanged", this::on_lockState_changed);
		on_lockState_changed();
		
		Signals.subscribeToEvent("onAudioSet", this::check_main_icon);
		Signals.subscribeToEvent("onQueueLibChanged", this::check_main_icon);
		Signals.subscribeToEvent("onQueueSet", this::check_main_icon);
		Signals.subscribeToEvent("onSongFinished", this::check_main_icon);
		check_main_icon();
	}

	private void mainClick(){
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.AUDIO || Action.get_lockState() == Action.LockState.DISK_ELEMENT) return; // If locked, do nothing
		Global.setAudio(music, true);
	}

	private void secondClick() {
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.AUDIO || Action.get_lockState() == Action.LockState.DISK_ELEMENT) return; // If locked, do nothing
		Action.set_element(music);
	}

	private void on_lockState_changed(){
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.AUDIO || Action.get_lockState() == Action.LockState.DISK_ELEMENT) {
			block.setAlpha(0.5f);
		}
		else {
			block.setAlpha(1f);
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		if (music == Action.get_element() && music != null){
			Action.unset_element();
		}
		super.onDetachedFromWindow();
	}
}
