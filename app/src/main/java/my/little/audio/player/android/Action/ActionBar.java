package my.little.audio.player.android.Action;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPL License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Context;

import android.util.AttributeSet;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.Signals;

public class ActionBar extends LinearLayout {
	private View action_general;
	private View action_music;
	private View action_folder;
	private View action_move_dialog;

	private DiskElement original_element;
	
	public ActionBar(Context context) {
		super(context);
		init(context);
	}
	
	public ActionBar(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public ActionBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	
	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.action_bar_layout, this, true);
		
		// Get the blocks
		action_general = findViewById(R.id.action_general);
		action_music = findViewById(R.id.action_music);
		action_folder = findViewById(R.id.action_folder);
		action_move_dialog = findViewById(R.id.action_move_dialog);
		
		// Link the general nodes
		findViewById(R.id.action_general_add_music).setOnClickListener(view -> Action.open_new_audio_file_dialog());
		findViewById(R.id.action_general_add_folder).setOnClickListener(view -> Action.request_system_folder_name(context));
		findViewById(R.id.action_general_refresh).setOnClickListener(view -> Action.button_refresh());
		findViewById(R.id.action_general_set_library).setOnClickListener(view -> Action.open_new_lib_root_dialog());
		findViewById(R.id.action_general_settings).setOnClickListener(view -> Signals.emitSignal("onTogglePreferences"));
		
		// Link the Element nodes
		findViewById(R.id.action_folder_back).setOnClickListener(view -> Action.unset_element());
		findViewById(R.id.action_music_back).setOnClickListener(view -> Action.unset_element());

		findViewById(R.id.action_folder_rename).setOnClickListener(view -> Action.rename_element(context));
		findViewById(R.id.action_music_rename).setOnClickListener(view -> Action.rename_element(context));

		findViewById(R.id.action_music_move).setOnClickListener(view -> init_file_moving());
		findViewById(R.id.action_folder_move).setOnClickListener(view -> init_file_moving());

		findViewById(R.id.action_folder_trash).setOnClickListener(view -> Action.delete_element());
		findViewById(R.id.action_music_trash).setOnClickListener(view -> Action.delete_element());

		// Link the Move dialog nodes.
		action_move_dialog.setOnClickListener(view -> finish_file_moving());
		
		Signals.subscribeToEvent("onActionElementChanged", this::onActionSet);
		onActionSet();
	}
	
	private void onActionSet() {
		// If you are currently performing a move, DO NOT do anything.
		if (action_move_dialog.getVisibility() == VISIBLE && original_element != null) return;

		if (Action.get_element() == null){
			action_general.setVisibility(VISIBLE);
			action_music.setVisibility(GONE);
			action_folder.setVisibility(GONE);
			action_move_dialog.setVisibility(GONE);
		} else if (Action.get_element() instanceof Directory) {
			action_general.setVisibility(GONE);
			action_music.setVisibility(GONE);
			action_folder.setVisibility(VISIBLE);
			action_move_dialog.setVisibility(GONE);
		} else if (Action.get_element() instanceof Music) {
			action_general.setVisibility(GONE);
			action_music.setVisibility(VISIBLE);
			action_folder.setVisibility(GONE);
			action_move_dialog.setVisibility(GONE);
		} else {
			action_general.setVisibility(VISIBLE);
			action_music.setVisibility(GONE);
			action_folder.setVisibility(GONE);
			action_move_dialog.setVisibility(GONE);
			Log.e(Global.APP_TAG, "This state is very confusing and should not happen. Action.get_element() is of type:" + Action.get_element().getClass().getName());
			Action.unset_element();
		}
	}

	private void init_file_moving(){
		action_move_dialog.setVisibility(VISIBLE);
		action_general.setVisibility(GONE);
		action_music.setVisibility(GONE);
		action_folder.setVisibility(GONE);
		Action.set_lockState(Action.LockState.AUDIO);
		original_element = Action.get_element();
		Action.unset_element();
	}

	private void finish_file_moving() {
		action_move_dialog.setVisibility(GONE);
		action_general.setVisibility(VISIBLE);
		Action.set_lockState(Action.LockState.NONE);
		if (original_element == null) {
			Log.e(Global.APP_TAG, "This state is very confusing and should not happen. original_element is null");
			return;
		}
		Action.move_file(original_element, Global.getPath());
		original_element = null;
	}
}
