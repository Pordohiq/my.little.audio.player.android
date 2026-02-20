package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

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
	
	//region MXState
	private ImageView active_mxs_repeat;
	private ImageView active_mxs_queue;
	private ImageView active_mxs_shuffle;
	
	private void MXInit(){
		active_mxs_repeat = findViewById(R.id.active_mxs_repeat);
		active_mxs_queue = findViewById(R.id.active_mxs_queue);
		active_mxs_shuffle = findViewById(R.id.active_mxs_shuffle);
		
		Signals.subscribeToEvent("onMxStateChanged", this::update_mx_buttons);
		
		active_mxs_repeat.setOnClickListener(view -> Global.mx_state.toggle_repeat_state());
		active_mxs_queue.setOnClickListener(view -> Global.mx_state.toggle_queue_state());
		active_mxs_shuffle.setOnClickListener(view -> Global.mx_state.toggle_shuffle_state());
	}
	
	private void update_mx_buttons(){
		MixingState.repeat_state repeat_state = Global.mx_state.get_repeat_state();
		MixingState.queue_state queue_state = Global.mx_state.get_queue_state();
		MixingState.shuffle_state shuffle_state = Global.mx_state.get_shuffle_state();
		
		if (repeat_state == MixingState.repeat_state.NONE){
			active_mxs_repeat.setImageResource(R.drawable.active_mxs_repeat);
		} else if (repeat_state == MixingState.repeat_state.ONE) {
			active_mxs_repeat.setImageResource(R.drawable.active_mxs_repeat_file);
		} else if (repeat_state == MixingState.repeat_state.QUEUE) {
			active_mxs_repeat.setImageResource(R.drawable.active_mxs_repeat_active);
		}
		
		if (queue_state == MixingState.queue_state.NONE){
			active_mxs_queue.setImageResource(R.drawable.active_mxs_queue);
		} else if (queue_state == MixingState.queue_state.LOADED_QUEUE){
			active_mxs_queue.setImageResource(R.drawable.active_mxs_queue_active);
		} else if (queue_state == MixingState.queue_state.DIRECTORY) {
			active_mxs_queue.setImageResource(R.drawable.active_mxs_folder_queue);
		} else if (queue_state == MixingState.queue_state.RECURSIVE_DIRECTORY) {
			active_mxs_queue.setImageResource(R.drawable.active_mxs_folder_queue_recursive);
		}
		
		active_mxs_shuffle.setImageResource(shuffle_state == MixingState.shuffle_state.NONE ? R.drawable.active_mxs_shuffle : R.drawable.active_mxs_shuffle_active);
	}
	//endregion
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
				
				if (Global.getPlayBackState() == Global.PlayBackState.PLAYING) {
					secondHandler.postDelayed(this, 100);
				}
			} catch (Exception ex) {
				secondHandler.removeCallbacks(this);
				Log.e(Global.APP_TAG, "Error: " + ex);
			}
		}
	};
	
	@NonNull
	private static String formatDuration(int totalSeconds) {
		int h = totalSeconds / 3600;
		int m = (totalSeconds % 3600) / 60;
		int s = totalSeconds % 60;
		
		if (h > 0) {
			return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s);
		} else {
			return String.format(Locale.getDefault(), "%02d:%02d", m, s);
		}
	}
	
	private void seekBarInit() {
		progressBar = findViewById(R.id.progressBar);
		timeInNode = findViewById(R.id.time_in);
		timeRemainingNode = findViewById(R.id.time_remaining);
		
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
		
		seekBarInit();
		MXInit();
		
		// Hook up all the Signals
		playButton.setOnClickListener(view -> {
			if (Global.getPlayBackState() == Global.PlayBackState.PLAYING){
				Global.setPlayBackState(Global.PlayBackState.PAUSED);
			} else if (Global.getPlayBackState() == Global.PlayBackState.PAUSED) {
				Global.setPlayBackState(Global.PlayBackState.PLAYING);
			}
			// TODO: Get the new song, if the pb state is NONE
		});
		
		Signals.subscribeToEvent("onPBStateChanged", this::onPbStateChanged);
		Signals.subscribeToEvent("onAudioSet", this::onAudioSet);
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
