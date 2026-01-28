package my.little.audio.player.android.Action;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.Signals;

public class Action {
	@Nullable
	private static DiskElement currentElement = null;
	
	public Action() {
		Signals.createEvent("onActionSet");
		Signals.createEvent("requestLibRootPathFromSysDialog");
		Signals.subscribeToEvent("onPathChanged", this::onPathChanged);
	}
	
	private void onPathChanged() {
		currentElement = null;
	}
	
	public static DiskElement get_element() { return currentElement; }
	
	public static void set_element(@NonNull DiskElement element) {
		Log.i(Global.APP_TAG, "Setting element: " + element.getName());
		currentElement = element;
		Signals.emitSignal("onActionSet");
	}
	
	public static void button_refresh() {
		Log.i(Global.APP_TAG, "Refreshing library");
		Global.path.clear();
		ResTree.reload_from_disk(Global.getInstance());
		Signals.emitSignal("onPathChanged");
	}
	
	public static void open_new_lib_root_dialog(){
		Log.i(Global.APP_TAG, "Opening new library root dialog");
		Signals.emitSignal("requestLibRootPathFromSysDialog");
	}
	
	public static void set_new_lib_root_path(Uri path){
		Log.i(Global.APP_TAG, "Setting new library root path: " + path);
		Global.path.clear();
		ResTree.set_library_path(path);
		ResTree.reload_from_disk(Global.getInstance());
		Signals.emitSignal("onPathChanged");
	}
	
	public static void open_new_audio_file_dialog(){
		Log.i(Global.APP_TAG, "Opening new audio file dialog");
		Signals.emitSignal("requestAudioFromSysDialog");
	}
	
	public static void import_music(Uri path) {
		// TODO: Fix this Uri path nonsense
		// Why is it so hard to just MOVE A FILE WITH SAF?
	}
	
	public static void request_system_folder_name(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Enter text");
		
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
			Log.i(Global.APP_TAG, "Result: " + result);
			create_new_folder(ResTree.get_folder_uri(Global.path), result);
		});
		
		builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
		
		builder.show();
	}
	
	public static void create_new_folder(Uri parent, String folderName) {
		Uri treeUri = parent;
		ContentResolver resolver = Global.getInstance().getContentResolver();
		Uri docUri = DocumentsContract.buildDocumentUriUsingTree(
				treeUri,
				DocumentsContract.getTreeDocumentId(treeUri)
		);
		
		try {
			DocumentsContract.createDocument(
					resolver,
					docUri,
					DocumentsContract.Document.MIME_TYPE_DIR,
					folderName
			);
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error creating new folder", e);
		}
	}
}
