package my.little.audio.player.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import my.little.audio.player.android.FileView.FileView;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.ResTree;

public class Main extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public static void enterSubfolder(Directory folder) {
		Global.path.add(folder.getName());
		ResTree.current_folder = ResTree.load_folder(Global.path);
	}
}
