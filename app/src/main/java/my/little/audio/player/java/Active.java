package my.little.audio.player.java;

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class Active extends LinearLayout {
	private TextView title;
	
	private ImageView previousButton;
	private ImageView playButton;
	private ImageView nextButton;
	
	//region INIT
	public Active(Context context) {
		super(context);
		init(context);
	}
	
	public Active(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.active_view_layout, this, true);
		title = findViewById(R.id.playing_title);
		
		previousButton = findViewById(R.id.previousButton);
		playButton = findViewById(R.id.playButton);
		nextButton = findViewById(R.id.nextButton);
		// Hook up all the Signals
		playButton.setOnClickListener(view -> Global.togglePlayback());
		Signals.subscribeToEvent(Signals.SignalType.PB_STATE_CHANGED, this::onPbStateChanged);
		Signals.subscribeToEvent(Signals.SignalType.AUDIO_SET, this::onAudioSet);
	}
	//endregion
	private void onPbStateChanged() {
		if(Global.current_playbackState == Global.PlayBackState.PLAYING) {
			playButton.setImageResource(R.drawable.active_pb_pause);
		} else {
			playButton.setImageResource(R.drawable.active_pb_play);
		}
	}
	
	private void onAudioSet() {
		title.setText(Global.current_audio.getName());
	}
	
	
}
