package my.little.audio.player.java.FileView;

import android.content.Context;

import android.util.AttributeSet;
import android.util.Log;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import my.little.audio.player.java.Global;

import my.little.audio.player.java.ResTree.ResTree;
import my.little.audio.player.java.ResTree.DiskElement;
import my.little.audio.player.java.ResTree.Directory;
import my.little.audio.player.java.ResTree.Music;

import my.little.audio.player.java.R;

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
	}
	
	public void updateFileView () {
		// Set the new PATH string
		List<String> new_path = Global.path;
		String path_formatted = getContext().getString(R.string.filePath) + String.join("/", new_path);
		pathDisplay.setText(path_formatted);
		// Load the folder
		List<DiskElement> elements = ResTree.load_folder(new_path);
		for (DiskElement element : elements) {
			if (element instanceof Directory) {
				FolderBlock block = new FolderBlock(getContext());
				block.setUp((Directory) element);
				fileContainer.addView(block);
			} else if (element instanceof Music) {
				MusicBlock block = new MusicBlock(getContext());
				block.setUp((Music) element);
				fileContainer.addView(block);
			} else {
				Log.w(Global.APP_TAG, "Unknown element type: " + element.getClass().getName());
			}
		}
	}
}