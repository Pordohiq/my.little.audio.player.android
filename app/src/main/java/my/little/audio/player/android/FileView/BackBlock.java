package my.little.audio.player.android.FileView;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the MIT License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.LinearLayout;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.R;
import my.little.audio.player.android.Global;
import my.little.audio.player.android.Signals;

public class BackBlock extends LinearLayout {
	private LinearLayout block;

	public BackBlock(Context context) {
		super(context);
		init(context);
	}
	
	public BackBlock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.back_block_layout, this, true);
		block = findViewById(R.id.back_bar_container);
		// Onclick
		block.setOnClickListener(view -> this.mainClick());
		// Signals
		Signals.subscribeToEvent("onLockStateChanged", this::on_lockState_changed);
		on_lockState_changed();
	}

	private void mainClick(){
		if (Action.get_lockState() == Action.LockState.ALL) return; // If locked, do nothing
		Global.leaveSubfolder();
	}

	private void on_lockState_changed(){
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.FOLDER) {
			block.setAlpha(0.5f);
		}
		else {
			block.setAlpha(1f);
		}
	}
}
