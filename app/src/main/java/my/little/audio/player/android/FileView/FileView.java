package my.little.audio.player.android.FileView;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the MIT License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Context;

import android.util.AttributeSet;
import android.util.Log;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.Music;

import my.little.audio.player.android.R;

public class FileView extends LinearLayout {
	private TextView pathDisplay;
	private LinearLayout fileContainer;
	
	public FileView(Context context) {
		super(context);
		init(context);
	}
	
	public FileView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.file_view_layout, this, true);
		pathDisplay = findViewById(R.id.pathDisplay);
		fileContainer = findViewById(R.id.fileContainer);
		updateFileView();
		Signals.subscribeToEvent("onPathChanged", this::updateFileView);
	}
	
	public void updateFileView () {
		if (ResTree.library == null) {
			return;
		}
		fileContainer.removeAllViews();
		// Set the new PATH string
		String path_formatted = getContext().getString(R.string.file_view_filePath) + String.join("/", Global.getPath());
		pathDisplay.setText(path_formatted);
		// Load the folder
		if (!Global.getPath().isEmpty()){
			fileContainer.addView(new BackBlock(getContext()));
		}
		for (DiskElement element : ResTree.current_folder) {
			
			if (element instanceof Directory) {
				FolderBlock block = new FolderBlock(getContext());
				block.setUp((Directory) element);
				fileContainer.addView(block);
			}
			
			else if (element instanceof Music) {
				MusicBlock block = new MusicBlock(getContext());
				block.setUp((Music) element);
				fileContainer.addView(block);
			}
			
			else {
				Log.w(Global.APP_TAG, "Unknown element type: " + element.getClass().getName());
			}
		}
	}
}
