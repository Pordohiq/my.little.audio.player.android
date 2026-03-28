package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.Prefrences.Preferences;
import my.little.audio.player.android.ResTree.ResTree;

public class Main extends ComponentActivity {
	private Preferences preferences;
	
	private void toggle_preferences() {
		Log.w(Global.APP_TAG, "Toggling preferences" + preferences.getHeight());
		if (preferences.getVisibility() == View.GONE){
			preferences.setVisibility(View.VISIBLE);
		} else {
			preferences.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Signals.createEvent("onTogglePreferences");
		
		Signals.subscribeToEvent("onTogglePreferences", this::toggle_preferences);
		Signals.subscribeToEvent("requestLibRootPathFromSysDialog", this::request_library_root_from_sys_dialog);
		Signals.subscribeToEvent("requestAudioFromSysDialog", this::request_audio_from_sys_dialog);
		
		preferences = findViewById(R.id.preferences);
		
		if (ResTree.library_root == null){
			Action.open_new_lib_root_dialog();
		}
		
		//Set up Back Button
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (Action.get_element() != null) {
					Action.unset_element();
				} else if (Action.get_queue() != null){
					Action.unset_queue();
				} else if (Global.getDisplayState() == Global.DisplayState.QUEUE_CONTENT) {
					Global.setDisplayState(Global.DisplayState.QUEUES);
				} else if (Global.getDisplayState() == Global.DisplayState.QUEUES) {
					Global.setDisplayState(Global.DisplayState.DISK_ELEMENT);
				} else if (!Global.getPath().isEmpty()) {
					Global.leaveSubfolder();
				} else {
					finish();
				}
			}
		});
		
		Signals.emitSignal("onPBStateChanged");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Signals.emitSignal("onPBStateChanged");
	}
	
	//region FileAccess requests
	private final ActivityResultLauncher<Uri> openDocumentTreeLauncher =
			registerForActivityResult(
					new ActivityResultContracts.OpenDocumentTree(),
					treeUri -> {
						if (treeUri != null) {
							final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
							getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
							Action.set_new_lib_root_path(treeUri);
						}
					});
	
	private final ActivityResultLauncher<String[]> openDocumentLauncher =
			registerForActivityResult(
					new ActivityResultContracts.OpenMultipleDocuments(),
					uris -> {
						if (uris != null && !uris.isEmpty()) {
							final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
							
							for (Uri uri : uris) {
								getContentResolver().takePersistableUriPermission(uri, takeFlags);
								Action.import_music(uri);
							}
						}
					});
	
	private void request_library_root_from_sys_dialog() {
		openDocumentTreeLauncher.launch(null);
	}
	
	private void request_audio_from_sys_dialog() {
		openDocumentLauncher.launch(new String[]{"audio/*"});
	}
	//endregion
}
