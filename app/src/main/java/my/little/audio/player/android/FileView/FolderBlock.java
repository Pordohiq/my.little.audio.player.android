package my.little.audio.player.android.FileView;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the MIT License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.Signals;

public class FolderBlock extends LinearLayout {
	// Nodes
	private LinearLayout block;
	private ImageView folderIcon;
	private TextView folderName;
	private ImageView moreIcon;
	// Data
	private Directory folder;
	
	public FolderBlock(Context context) {
		super(context);
		init(context);
	}
	
	public FolderBlock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.folder_block_layout, this, true);
		block = findViewById(R.id.folder_block);

		folderIcon = findViewById(R.id.folder_icon);
		folderName = findViewById(R.id.folder_name);
		moreIcon = findViewById(R.id.more_icon);
		
		Signals.subscribeToEvent("onActionElementChanged", this::check_more_icon);
		Signals.subscribeToEvent("onLockStateChanged", this::on_lockState_changed);
		on_lockState_changed();
	}
	
	private void check_more_icon() {
		if (folder == Action.get_element() && folder != null) {
			moreIcon.setImageResource(R.drawable.block_more_active);
		} else {
			moreIcon.setImageResource(R.drawable.block_more);
		}
	}
	
	public void setUp(Directory dir) {
		folder = dir;
		folderName.setText(folder.getName());
		// Onclick
		folderIcon.setOnClickListener(view -> mainClick());
		folderName.setOnClickListener(view -> mainClick());
		moreIcon.setOnClickListener(view -> secondClick());
		// Connection with ACTION
		check_more_icon();
		
	}

	private void mainClick(){
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.FOLDER) return; // If locked, do nothing
		Global.enterSubfolder(folder);
	}

	private void secondClick() {
		if (Action.get_lockState() == Action.LockState.ALL || Action.get_lockState() == Action.LockState.FOLDER) return; // If locked, do nothing
		Action.set_element(folder);
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
