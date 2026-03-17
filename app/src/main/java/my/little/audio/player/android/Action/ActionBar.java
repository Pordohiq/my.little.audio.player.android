package my.little.audio.player.android.Action;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPL License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Context;

import android.util.AttributeSet;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.MixingState;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.queues.Queues;

public class ActionBar extends LinearLayout {
	private View action_general;
	private View action_music;
	private View action_folder;
	private View action_move_dialog;
	
	private TextView queue_title_name;
	
	private View add_music_button;
	private View add_folder_button;
	private View add_queue_button;
	
	private ImageView action_music_toggle_queue;

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
		
		// Get the buttons
		add_music_button = findViewById(R.id.action_general_add_music);
		add_folder_button = findViewById(R.id.action_general_add_folder);
		add_queue_button = findViewById(R.id.action_general_add_queue);
		
		action_music_toggle_queue = findViewById(R.id.action_music_toggle_queue);
		
		queue_title_name = findViewById(R.id.action_queue_title_name);
		
		// Link the general nodes
		add_music_button.setOnClickListener(view -> Action.open_new_audio_file_dialog());
		add_folder_button.setOnClickListener(view -> Action.request_system_folder_name(context));
		add_queue_button.setOnClickListener(view -> Action.request_system_queue_name(context));
		
		findViewById(R.id.action_general_refresh).setOnClickListener(view -> Action.button_refresh());
		findViewById(R.id.action_general_set_library).setOnClickListener(view -> Action.open_new_lib_root_dialog());
		findViewById(R.id.action_general_settings).setOnClickListener(view -> Signals.emitSignal("onTogglePreferences"));
		
		// Link the Element nodes
		findViewById(R.id.action_folder_back).setOnClickListener(view -> Action.unset_element());
		findViewById(R.id.action_music_back).setOnClickListener(view -> Action.unset_element());

		findViewById(R.id.action_folder_rename).setOnClickListener(view -> Action.rename_element(context));
		findViewById(R.id.action_music_rename).setOnClickListener(view -> Action.rename_element(context));

		action_music_toggle_queue.setOnClickListener(view -> toggle_queue());
		
		findViewById(R.id.action_music_move).setOnClickListener(view -> init_file_moving());
		findViewById(R.id.action_folder_move).setOnClickListener(view -> init_file_moving());

		findViewById(R.id.action_folder_trash).setOnClickListener(view -> Action.delete_element());
		findViewById(R.id.action_music_trash).setOnClickListener(view -> Action.delete_element());

		// Link the Move dialog nodes.
		action_move_dialog.setOnClickListener(view -> finish_file_moving());
		
		Signals.subscribeToEvent("onActionElementChanged", this::onActionSet);
		onActionSet();
		Signals.subscribeToEvent("onDisplayStateChanged", this::onDisplayStateChanged);
		onDisplayStateChanged();
		Signals.subscribeToEvent("onQueueLibChanged", this::verify_add_to_queue_button);
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
			
			verify_add_to_queue_button();
		} else {
			action_general.setVisibility(VISIBLE);
			action_music.setVisibility(GONE);
			action_folder.setVisibility(GONE);
			action_move_dialog.setVisibility(GONE);
			Log.e(Global.APP_TAG, "This state is very confusing and should not happen. Action.get_element() is of type:" + Action.get_element().getClass().getName());
			Action.unset_element();
		}
	}
	
	private void onDisplayStateChanged(){
		Action.unset_element();
		if (Global.getDisplayState() == Global.DisplayState.DISK_ELEMENT){
			add_queue_button.setVisibility(GONE);
			queue_title_name.setVisibility(GONE);
			add_music_button.setVisibility(VISIBLE);
			add_folder_button.setVisibility(VISIBLE);
			action_general.setVisibility(VISIBLE);
		} else if (Global.getDisplayState() == Global.DisplayState.QUEUE_CONTENT) {
			queue_title_name.setVisibility(VISIBLE);
			if (Queues.get_active_queue() == null){
				Log.e(Global.APP_TAG, "The Display State is set to Queue Content but there is no active queue...");
				return;
			}
			queue_title_name.setText(Queues.get_active_queue().get_name());
			add_queue_button.setVisibility(GONE);
			add_music_button.setVisibility(GONE);
			action_general.setVisibility(GONE);
			add_folder_button.setVisibility(GONE);
		} else {
			add_queue_button.setVisibility(VISIBLE);
			action_general.setVisibility(VISIBLE);
			add_music_button.setVisibility(GONE);
			add_folder_button.setVisibility(GONE);
			queue_title_name.setVisibility(GONE);
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
	
	private void verify_add_to_queue_button(){
		boolean can_add_to_queue = Global.mx_state.get_queue_state() == MixingState.queue_state.LOADED_QUEUE && Queues.get_active_queue() != null;
		if(can_add_to_queue) {
			action_music_toggle_queue.setAlpha(1f);
		} else {
			action_music_toggle_queue.setAlpha(0.5f);
		}
		
		if (can_add_to_queue && Action.get_element() instanceof Music && Queues.get_active_queue().has_song((Music) Action.get_element())) {
			action_music_toggle_queue.setImageResource(R.drawable.action_music_queue_remove);
		} else {
			action_music_toggle_queue.setImageResource(R.drawable.action_music_queue_add);
		}
	}
	
	private void toggle_queue() {
		if (!(Global.mx_state.get_queue_state() == MixingState.queue_state.LOADED_QUEUE && Queues.get_active_queue() != null) || Action.get_element() == null || Queues.get_active_queue() == null) return;
		Queues.add_music_to_queue((Music) Action.get_element(), Queues.get_active_queue());
	}
}
