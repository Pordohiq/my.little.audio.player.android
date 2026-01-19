package my.little.audio.player.android.FileView;

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.Signals;

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
		
		Signals.subscribeToEvent(Signals.SignalType.AUDIO_SET, this::onAudioSet);
	}
	
	private void onAudioSet() {
		if (music == Global.current_audio && music != null) {
			mainIcon.setImageResource(R.drawable.block_music_active);
		}
		// TODO: Elif for active queue or invalid audio
		else {
			mainIcon.setImageResource(R.drawable.block_music);
		}
	}
	
	public void setUp(Music mus) {
		music = mus;
		musicName.setText(music.getName());
		onAudioSet();
		// Hook up buttons
		mainIcon.setOnClickListener(view -> Global.setAudio(music, true));
		musicName.setOnClickListener(view -> Global.setAudio(music, true));
		moreIcon.setOnClickListener(view -> {
			// TODO: Hook up to action
		});
	}
}
