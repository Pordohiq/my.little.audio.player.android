package my.little.audio.player.android.Action;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileNotFoundException;

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
	
	private static Uri getParentUri(@NonNull Uri childUri) {
		String docId = DocumentsContract.getDocumentId(childUri);
		int lastSlash = docId.lastIndexOf('/');
		if (lastSlash == -1) {
			// It's in the root of the volume (e.g., "primary:song.mp3")
			String volume = docId.split(":")[0];
			return DocumentsContract.buildDocumentUri(
					childUri.getAuthority(),
					volume + ":"
			);
		}
		
		String parentId = docId.substring(0, lastSlash);
		return DocumentsContract.buildDocumentUri(childUri.getAuthority(), parentId);
	}
	
	public static void import_music(Uri path){
		try {
			String sourceId = DocumentsContract.getDocumentId(path);
			String sourceParentId = DocumentsContract.getDocumentId(getParentUri(path));
			String destParentId = DocumentsContract.getDocumentId(ResTree.library_root);
			
			ContentResolver resolver = Global.getInstance().getContentResolver();
			
			// Perform the move
			Uri resultUri = DocumentsContract.moveDocument(
					resolver,
					path,
					getParentUri(path),
					ResTree.library_root
			);
			
			if (resultUri != null) {
				Log.d(Global.APP_TAG, "File moved successfully to: " + resultUri.toString());
			}
		} catch (FileNotFoundException | IllegalArgumentException e) {
			Log.e(Global.APP_TAG, "Move failed", e);
		}
	}
}
