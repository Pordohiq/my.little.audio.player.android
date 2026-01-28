package my.little.audio.player.android;

import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.ResTree.ResTree;

public class Main extends ComponentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Signals.subscribeToEvent("requestLibRootPathFromSysDialog", this::request_library_root_from_sys_dialog);
		Signals.subscribeToEvent("requestAudioFromSysDialog", this::request_audio_from_sys_dialog);
		
		if (ResTree.library_root == null){
			Action.open_new_lib_root_dialog();
		}
		
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
