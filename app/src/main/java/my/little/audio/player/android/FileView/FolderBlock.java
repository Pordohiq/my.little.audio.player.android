package my.little.audio.player.android.FileView;

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.Signals;

public class FolderBlock extends LinearLayout {
	// Nodes
	private ImageView folderIcon;
	private TextView folderName;
	private ImageView moreIcon;
	// Data
	private Directory folder;
	
	public FolderBlock(Context context) {
		super(context);
		init(context);
	}
	
	public FolderBlock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.folder_block_layout, this, true);
		folderIcon = findViewById(R.id.folder_icon);
		folderName = findViewById(R.id.folder_name);
		moreIcon = findViewById(R.id.more_icon);
		
		Signals.subscribeToEvent("onActionSet", this::check_more_icon);
	}
	
	private void check_more_icon() {
		if (folder == Action.get_element() && folder != null) {
			moreIcon.setImageResource(R.drawable.block_more_active);
		} else {
			moreIcon.setImageResource(R.drawable.block_more);
		}
	}
	
	public void setUp(Directory dir) {
		folder = dir;
		folderName.setText(folder.getName());
		// Onclick
		folderIcon.setOnClickListener(view -> Global.enterSubfolder(dir));
		folderName.setOnClickListener(view -> Global.enterSubfolder(dir));
		moreIcon.setOnClickListener(view -> Action.set_element(dir));
		// Connection with ACTION
		check_more_icon();
		
	}
	
}
