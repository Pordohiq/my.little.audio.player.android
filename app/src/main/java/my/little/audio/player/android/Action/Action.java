package my.little.audio.player.android.Action;

import android.util.Log;

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
		Signals.createEvent("requestPathFromSysDialog");
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
		Signals.emitSignal("requestPathFromSysDialog");
	}
	
	public static void set_new_lib_root_path(String path){
		Log.i(Global.APP_TAG, "Setting new library root path: " + path);
		Global.path.clear();
		ResTree.set_library_path(path);
		ResTree.reload_from_disk(Global.getInstance());
		Signals.emitSignal("onPathChanged");
	}
}
