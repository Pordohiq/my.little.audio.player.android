package my.little.audio.player.android.fileInfo;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.Signals;

public class fileInfoDirectory extends LinearLayout {
	private TextView subaudiofiles;
	private TextView subdirs;
	private TextView rec_size;
	
	private Activity activity;
	
	public fileInfoDirectory(Context context) {
		super(context);
		init(context);
	}
	
	public fileInfoDirectory(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		activity = (Activity) context;
		
		LayoutInflater.from(context).inflate(R.layout.info_directory, this, true);
		subaudiofiles = findViewById(R.id.info_directory_audio);
		subdirs = findViewById(R.id.info_directory_dirs);
		rec_size = findViewById(R.id.info_directory_rec_file_size);
		hide();
		Signals.subscribeToEvent("requestShowInfoDir", this::load_data);
		
		Signals.subscribeToEvent("onPathChanged", this::hide);
		Signals.subscribeToEvent("onActionElementChanged", this::hide);
		Signals.subscribeToEvent("onActionQueueChanged", this::hide);
	}
	
	public void load_data(){
		if (this.getVisibility() == VISIBLE){
			hide();
			return;
		}
		
		DiskElement de = Action.get_element();
		if (!(de instanceof Directory)) {
			hide();
			return;
		}
		List<String> path = ResTree.get_local_element_path(de, null, null);
		if (path == null){
			hide();
			return;
		}
		
		Global.executor.execute(() -> {
			String subfolders = Global.getInstance().getString(R.string.info_directory_dirs) + ResTree.get_dirs_at_path(path, true).toArray().length;
			activity.runOnUiThread(() -> subdirs.setText(subfolders));
		});
		
		Global.executor.execute(() -> {
			String subaudios = Global.getInstance().getString(R.string.info_directory_audio) + ResTree.get_musics_at_path(path, true).toArray().length;
			activity.runOnUiThread(() -> this.subaudiofiles.setText(subaudios));
		});
		
		Global.executor.execute(() -> {
			String rfs = Global.getInstance().getText(R.string.info_directory_rec_file_size) +  String.valueOf(ResTree.getDirectorySize(de.getUri())) + " B";
			activity.runOnUiThread(() -> rec_size.setText(rfs));
		});
		
		
		
		
		this.setVisibility(VISIBLE);
	}
	
	private void hide(){
		this.setVisibility(GONE);
	}
}
