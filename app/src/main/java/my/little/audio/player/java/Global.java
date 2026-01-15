package my.little.audio.player.java;

import my.little.audio.player.java.ResTree.ResTree;
import my.little.audio.player.java.ResTree.Directory;

import android.app.Application;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Global extends Application {
	public static final String APP_TAG = "myLittleAudioPlayer";
	private static Global instance;
	
	public static List<String> path = new ArrayList<>();
	
	public static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
			// Common Lossy
			"mp3", "aac", "m4a", "ogg", "wma", "opus",
			// Common Lossless
			"flac", "wav", "alac", "aiff",
			// Other/Less common
			"ape", "mpc", "wv", "ra", "rm", "amr", "mid"
	);
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		// Load the Resource Tree
		ResTree.init("/storage/emulated/0/Music");
		
		Log.i(APP_TAG, "Welcome to my LittleAudioPlayer");
		Log.d(APP_TAG, ResTree.library.toString());
	}
	
	public static Global getInstance() {
		return instance;
	}
	
	public static void enterSubfolder(Directory subfolder) {
		path.add(subfolder.getName());
		ResTree.current_folder = ResTree.load_folder(path);
		Signals.emitSignal(Signals.SignalType.PATH_CHANGED);
	}
	
	public static void leaveSubfolder() {
		path.remove(path.size() - 1);
		ResTree.current_folder = ResTree.load_folder(path);
		Signals.emitSignal(Signals.SignalType.PATH_CHANGED);
	}
}
