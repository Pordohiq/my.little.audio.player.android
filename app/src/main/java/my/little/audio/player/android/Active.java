package my.little.audio.player.android;

import android.content.Context;

import android.os.Handler;

import android.util.AttributeSet;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Active extends LinearLayout {
	private TextView title;
	
	private ImageView previousButton; // TODO: Hook it up
	private ImageView playButton;
	private ImageView nextButton; // TODO: Hook it up as well
	
	//region ProgressBar
	private SeekBar progressBar;
	private TextView timeInNode;
	private TextView timeRemainingNode;
	private final Handler secondHandler = new Handler();
	private final Runnable secondLoop = new Runnable() { // This loop runs every second, so the
		@Override
		public void run() {
			try {
				progressBar.setProgress(Global.getPlayBackPosition());
				progressBar.setMax(Global.getPlayBackDuration());
				
				timeInNode.setText(formatDuration(Global.getPlayBackPosition()));
				timeRemainingNode.setText(formatDuration(Global.getPlayBackDuration() - Global.getPlayBackPosition()));
				
				secondHandler.postDelayed(this, 100);
			} catch (Exception ex) {
				secondHandler.removeCallbacks(this);
				Log.e(Global.APP_TAG, "Error: " + ex);
			}
		}
	};
	
	@NonNull
	public static String formatDuration(int totalSeconds) {
		int h = totalSeconds / 3600;
		int m = (totalSeconds % 3600) / 60;
		int s = totalSeconds % 60;
		
		if (h > 0) {
			return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s);
		} else {
			return String.format(Locale.getDefault(), "%02d:%02d", m, s);
		}
	}
	//endregion
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
		
		progressBar = findViewById(R.id.progressBar);
		timeInNode = findViewById(R.id.time_in);
		timeRemainingNode = findViewById(R.id.time_remaining);
		
		// Hook up all the Signals
		playButton.setOnClickListener(view -> {
			if (Global.getPlayBackState() == Global.PlayBackState.PLAYING){
				Global.setPlayBackState(Global.PlayBackState.PAUSED);
			} else if (Global.getPlayBackState() == Global.PlayBackState.PAUSED) {
				Global.setPlayBackState(Global.PlayBackState.PLAYING);
			}
			// TODO: Get the new song, if the pb state is NONE
		});
		progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					timeInNode.setText(formatDuration(progress));
					timeRemainingNode.setText(formatDuration(Global.getPlayBackDuration() - progress));
				}
			
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if (Global.getPlayBackState() == Global.PlayBackState.PLAYING) {
					secondHandler.removeCallbacks(secondLoop);
					Global.setPlayBackState(Global.PlayBackState.PAUSED);
				}
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (Global.getPlayBackState() == Global.PlayBackState.PAUSED) {
					Global.seekTo(seekBar.getProgress());
					Global.setPlayBackState(Global.PlayBackState.PLAYING);
					secondLoop.run();
				}
			}
		});
		Signals.subscribeToEvent(Signals.SignalType.PB_STATE_CHANGED, this::onPbStateChanged);
		Signals.subscribeToEvent(Signals.SignalType.AUDIO_SET, this::onAudioSet);
	}
	//endregion
	//region Signals
	private void onPbStateChanged() {
		if(Global.getPlayBackState() == Global.PlayBackState.PLAYING) {
			playButton.setImageResource(R.drawable.active_pb_pause);
			secondLoop.run();
		} else {
			playButton.setImageResource(R.drawable.active_pb_play);
			secondHandler.removeCallbacks(secondLoop);
		}
	}
	
	private void onAudioSet() {
		title.setText(Global.current_audio.getName());
		progressBar.setMax(Global.getPlayBackDuration());
		progressBar.setProgress(0);
	}
	//endregion
}
