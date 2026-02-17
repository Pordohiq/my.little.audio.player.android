package my.little.audio.player.android.Action;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPL License:
// https://github.com/lomjek/my.little.audio.player.android

import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.Signals;

public class Action {
	@Nullable
	private static DiskElement currentElement = null;

	//region lockState
	public enum LockState {
		NONE,
		AUDIO,
		FOLDER,
		ALL
	}
	private static LockState lockState = LockState.NONE;
	public static LockState get_lockState() { return lockState; }
	public static void set_lockState(LockState state) {
		lockState = state;
		Signals.emitSignal("onLockStateChanged");
	}
	//endregion
	public Action() {
		Signals.createEvent("onActionElementChanged");
		Signals.createEvent("onLockStateChanged");
		Signals.createEvent("requestLibRootPathFromSysDialog");

		Signals.subscribeToEvent("onPathChanged", Action::unset_element);
	}
	
	public static void unset_element() {
		Log.i(Global.APP_TAG, "Unsetting element");
		currentElement = null;
		Signals.emitSignal("onActionElementChanged");
	}
	
	public static DiskElement get_element() { return currentElement; }
	
	public static void set_element(@NonNull DiskElement element) {
		Log.i(Global.APP_TAG, "Setting element: " + element.getName());
		if (element == currentElement) {
			unset_element();
		} else {
			currentElement = element;
		}
		Signals.emitSignal("onActionElementChanged");
	}
	
	//region ActionBar_general functions
	public static void button_refresh() {
		Log.i(Global.APP_TAG, "Refreshing library");
		Global.setPath(new ArrayList<>());
		ResTree.reload_from_disk(Global.getInstance());
		Signals.emitSignal("onPathChanged");
	}
	
	public static void open_new_lib_root_dialog(){
		Log.i(Global.APP_TAG, "Opening new library root dialog");
		Signals.emitSignal("requestLibRootPathFromSysDialog");
	}
	
	public static void set_new_lib_root_path(Uri path){
		Log.i(Global.APP_TAG, "Setting new library root path: " + path);
		Global.setPath(new ArrayList<>());
		ResTree.set_library_path(path);
		ResTree.reload_from_disk(Global.getInstance());
		Signals.emitSignal("onPathChanged");
	}
	
	public static void open_new_audio_file_dialog(){
		Log.i(Global.APP_TAG, "Opening new audio file dialog");
		Signals.emitSignal("requestAudioFromSysDialog");
	}
	
	public static void import_music(Uri path) {
		ResTree.add_audio_file(path, Global.getPath());
	}
	
	public static void request_system_folder_name(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.action_popup_new_folder);
		
		final EditText input = new EditText(context);
		
		FrameLayout container = new FrameLayout(context);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT
		);
		params.setMargins(48, 20, 48, 20);
		input.setLayoutParams(params);
		container.addView(input);
		
		builder.setView(container);
		
		builder.setPositiveButton("OK", (dialog, which) -> {
			String result = input.getText().toString();
			Log.i(Global.APP_TAG, "New Folder Name: " + result);
			create_new_folder_at_path(result);
		});
		
		builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
		
		builder.show();
	}
	
	public static void create_new_folder_at_path(String folderName) {
		ResTree.create_new_folder(Global.getPath(), folderName);
	}
	
	public static void reset_app_data(){
		ActivityManager am = (ActivityManager) Global.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
		if (am != null) {
			am.clearApplicationUserData();
		}
	}
	//endregion
	//region ActionBar_element functions
	public static void move_file(@NonNull DiskElement element, @NonNull List<String> path) {
		Log.i(Global.APP_TAG, "Moving file " + element.getName() + " to new path: " + String.join("/", path));
		ResTree.move_file(element, path);
		unset_element();
		Signals.emitSignal("onPathChanged");
	}

	public static void delete_element() {
		Log.i(Global.APP_TAG, "Deleting element");
		ResTree.delete_file(get_element());
		unset_element();
		Signals.emitSignal("onPathChanged");
	}
	//endregion
}
