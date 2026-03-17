package my.little.audio.player.android.FileView;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Context;

import android.util.AttributeSet;
import android.util.Log;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.queues.Queue;

import my.little.audio.player.android.R;
import my.little.audio.player.android.queues.Queues;

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
		Signals.subscribeToEvent("onDisplayStateChanged", this::updateFileView);
		Signals.subscribeToEvent("onQueueLibChanged", this::updateFileView);
	}
	
	public void updateFileView () {
		if (Global.getDisplayState() == Global.DisplayState.QUEUES){
			draw_queues();
		} else if (Global.getDisplayState() == Global.DisplayState.DISK_ELEMENT) {
			draw_diskElements();
		} else if (Global.getDisplayState() == Global.DisplayState.QUEUE_CONTENT) {
			draw_queue_contents();
		} else {
			Log.e(Global.APP_TAG, "Unknown display state: " + Global.getDisplayState() + " in FileView");
		}
	}
	
	private void draw_queues(){
		fileContainer.removeAllViews();
		
		String path_formatted = getContext().getString(R.string.file_view_queuePath) + String.join("/", Global.getPath());
		pathDisplay.setText(path_formatted);
		
		BackBlock backBlock = new BackBlock(getContext());
		backBlock.override_mainClick(() -> Global.setDisplayState(Global.DisplayState.DISK_ELEMENT));
		fileContainer.addView(backBlock);
		
		for (Queue que : Queues.get_queues()) {
			QueueBlock block = new QueueBlock(getContext());
			block.setUp(que);
			fileContainer.addView(block);
		}
	}
	
	private void draw_diskElements(){
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
	
	private void draw_queue_contents(){
		fileContainer.removeAllViews();
		if (Queues.get_active_queue() == null) return;
		List<Music> songs = Queues.get_active_queue().get_songs();
		
		String formatted_path = getContext().getString(R.string.file_view_queuePath) + Queues.get_active_queue().get_name();
		pathDisplay.setText(formatted_path);
		
		BackBlock back = new BackBlock(getContext());
		back.override_mainClick(() -> Global.setDisplayState(Global.DisplayState.QUEUES));
		fileContainer.addView(back);
		
		for (Music element : songs) {
			MusicQueueBlock block = new MusicQueueBlock(getContext());
			block.setUp(element, Queues.get_active_queue());
			fileContainer.addView(block);
		}
	}
}
