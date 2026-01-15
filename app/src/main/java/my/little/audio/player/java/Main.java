package my.little.audio.player.java;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import my.little.audio.player.java.FileView.FileView;
import my.little.audio.player.java.ResTree.Directory;
import my.little.audio.player.java.ResTree.ResTree;

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