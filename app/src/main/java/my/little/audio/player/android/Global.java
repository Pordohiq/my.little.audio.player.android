package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

// @ {} [] # \ || !=

import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.queues.Queue;
import my.little.audio.player.android.queues.Queues;

import android.app.Application;

import android.content.ComponentName;

import android.util.Log;

import androidx.annotation.NonNull;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Global extends Application {
	public static final String APP_TAG = "myLittleAudioPlayer";
	private static Global instance;
	
	//region DisplayState
	public enum DisplayState {
		DISK_ELEMENT,
		QUEUES,
		QUEUE_CONTENT
	}
	private static DisplayState displayState = DisplayState.DISK_ELEMENT;
	public static DisplayState getDisplayState(){ return displayState; }
	public static void setDisplayState(DisplayState new_displayState){
		displayState = new_displayState;
		Signals.emitSignal("onDisplayStateChanged");
	}
	//endregion

	public static MixingState mx_state = new MixingState();

	public enum PlayBackState {
		PLAYING,
		PAUSED,
		NONE
	}
	private static PlayBackState current_playbackState = PlayBackState.NONE;
	
	// Status on current song
	public static Music current_audio;
	
	private static MediaController mediaController;
	private static final Player.Listener pb_listener  = new Player.Listener() {
		@Override
		public void onPlaybackStateChanged(int playbackState) {
			Player.Listener.super.onPlaybackStateChanged(playbackState);
			current_playbackState = mapCustomPBState(playbackState, mediaController.getPlayWhenReady());
			Signals.emitSignal("onPBStateChanged");
		}
		
		@Override
		public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
			Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
			current_playbackState = mapCustomPBState(mediaController.getPlaybackState(), playWhenReady);
			Signals.emitSignal("onPBStateChanged");
		}
	};
	
	public static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
			// Common Lossy
			"mp3", "aac", "m4a", "ogg", "wma", "opus", "oga",
			// Common Lossless
			"flac", "wav", "alac", "aiff",
			// Other/Less common
			"ape", "mpc", "wv", "ra", "rm", "amr", "mid"
	);
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		Signals.createEvent("onPathChanged");
		
		Signals.createEvent("onAudioSet");
		
		Signals.createEvent("onPBStateChanged");
		
		Signals.createEvent("onSongFinished");
		
		Signals.createEvent("onDisplayStateChanged");
		
		// Load the Resource Tree
		ResTree.init(this);
		
		// Load the Queues
		Queues.init();
		
		// Connect to the Service AudioPlayer
		SessionToken sessionToken = new SessionToken(this,
				new ComponentName(this, AudioPlayer.class));
		
		ListenableFuture<MediaController> controllerFuture =
				new MediaController.Builder(this, sessionToken).buildAsync();
		
		controllerFuture.addListener(() -> {
			try {
				// Now the controller is connected to your AudioPlayer service
				mediaController = controllerFuture.get();
				assert mediaController != null;
				mediaController.addListener(pb_listener);
			} catch (Exception ex) {
				Log.e(APP_TAG, "Fatal: " + ex);
				System.exit(1);
			}
		}, MoreExecutors.directExecutor());
		
		// Welcome the User
		Log.i(APP_TAG, "Welcome to my LittleAudioPlayer");
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		if (mediaController != null) {
			mediaController.removeListener(pb_listener);
		}
	}
	
	public static Global getInstance() { return instance; }
	
	private static PlayBackState mapCustomPBState(int exoState, boolean playWhenReady) {
		if (exoState == Player.STATE_IDLE) {
			return PlayBackState.NONE;
		} else if (exoState == Player.STATE_ENDED) {
			Signals.emitSignal("onSongFinished");
			play_next();
			return PlayBackState.NONE;
		} else if (exoState == Player.STATE_READY || exoState == Player.STATE_BUFFERING) {
			return playWhenReady ? PlayBackState.PLAYING : PlayBackState.PAUSED;
		} else {
			return PlayBackState.NONE;
		}
	}
	
	//region Path logic
	private static List<String> path = new ArrayList<>();
	
	public static List<String> getPath() {
		Log.d(APP_TAG, "Getting path: " + path);
		return path;
	}
	
	public static void setPath(List<String> new_path) {
		Log.d(APP_TAG, "Setting path: " + new_path);
		path = new_path;
		Signals.emitSignal("onPathChanged");
	}
	
	public static void enterSubfolder(@NonNull Directory subfolder) {
		path.add(subfolder.getName());
		ResTree.current_folder = ResTree.load_folder(path);
		Signals.emitSignal("onPathChanged");
	}
	
	public static void leaveSubfolder() {
		Log.d(APP_TAG, "Leaving subfolder");
		path.remove(path.size() - 1);
		ResTree.current_folder = ResTree.load_folder(path);
		Signals.emitSignal("onPathChanged");
	}
	//endregion
	//region AudioLogic
	public static void setAudio(@NonNull Music audio, boolean playImmediately) {
		Global.current_audio = audio;
		mediaController.setMediaItem(
				new MediaItem.Builder().setUri(audio.getUri()).build()
		);
		mediaController.prepare();
		
		if (playImmediately) {
			mediaController.play();
		}
		Signals.emitSignal("onAudioSet");
	}
	
	public static void quit_song(){
		mediaController.stop();
		mediaController.clearMediaItems();
		setPlayBackState(PlayBackState.NONE);
		current_audio = null;
		Signals.emitSignal("onSongFinished");
	}
	
	public static void setPlayBackState(PlayBackState pbs){
		current_playbackState = pbs;
		if (pbs == PlayBackState.PLAYING)
			mediaController.play();
		else if (pbs == PlayBackState.PAUSED) {
			mediaController.pause();
		} else {
			mediaController.stop();
		}
		Signals.emitSignal("onPBStateChanged");
	}
	
	public static PlayBackState getPlayBackState(){
		return current_playbackState;
	}
	
	public static int getPlayBackPosition() {
		try {
			return Math.toIntExact(mediaController.getCurrentPosition() / 1000);
		} catch (ArithmeticException aex) {
			return 0;
		}
	}
	
	public static int getPlayBackDuration() {
		try {
			return Math.toIntExact(mediaController.getDuration() / 1000);
		}catch (ArithmeticException aex) {
			return 0;
		}
	}
	
	public static void play_next(){
		if(mx_state.get_queue_state() == MixingState.queue_state.NONE){
			if (mx_state.get_repeat_state() == MixingState.repeat_state.ONE){
				setAudio(current_audio, true);
			} else {
				quit_song();
			}
			return;
		}
		
		MixingState.queue_state[] queue_states = {MixingState.queue_state.DIRECTORY, MixingState.queue_state.RECURSIVE_DIRECTORY, MixingState.queue_state.LOADED_QUEUE};
		if (Arrays.asList(queue_states).contains(mx_state.get_queue_state())) {
			Queue cur_queue = Queues.get_active_queue();
			if (cur_queue == null) return;
			
			Music next_song;
			if (mx_state.get_repeat_state() == MixingState.repeat_state.NONE){
				next_song = cur_queue.get_next_song(false);
			} else if (mx_state.get_repeat_state() == MixingState.repeat_state.QUEUE) {
				next_song = cur_queue.get_next_song(true);
			} else {
				Log.e(APP_TAG, "Unknown repeat state: " + mx_state.get_repeat_state());
				quit_song();
				return;
			}
			
			
			if (next_song == null) {
				quit_song();
				return;
			}
			setAudio(next_song, true);
			return;
		}
		Log.e(Global.APP_TAG, "When you are here, something really weird must have happened...");
	}
	
	public static void seekTo(int seconds) {
		mediaController.seekTo(seconds * 1000L);
	}
	//endregion
}
