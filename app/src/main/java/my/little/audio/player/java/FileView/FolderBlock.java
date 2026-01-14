package my.little.audio.player.java.FileView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import my.little.audio.player.java.R;
import my.little.audio.player.java.ResTree.Directory;

public class FolderBlock extends LinearLayout {
	private TextView folderName;
	private ImageView moreIcon;
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
		folderName = findViewById(R.id.folder_name);
		moreIcon = findViewById(R.id.more_icon);
	}
	
	public void setUp(Directory dir) {
		folder = dir;
		folderName.setText(folder.getName());
	}
}
