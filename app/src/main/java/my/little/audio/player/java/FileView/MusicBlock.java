package my.little.audio.player.java.FileView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import my.little.audio.player.java.R;
import my.little.audio.player.java.ResTree.Music;

public class MusicBlock extends LinearLayout {
	private ImageView mainIcon;
	private TextView musicName;
	private ImageView moreIcon;
	
	private Music music;
	
	public MusicBlock(Context context) {
		super(context);
		init(context);
	}
	
	public MusicBlock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.music_block_layout, this, true);
		mainIcon = findViewById(R.id.music_icon);
		musicName = findViewById(R.id.music_name);
		moreIcon = findViewById(R.id.more_icon);
	}
	
	public void setUp(Music mus) {
		music = mus;
		musicName.setText(music.getName());
	}
}
