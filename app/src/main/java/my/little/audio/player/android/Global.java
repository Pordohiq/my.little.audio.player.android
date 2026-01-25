package my.little.audio.player.android;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.Music;

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
import java.util.List;

public class Global extends Application {
	public static final String APP_TAG = "myLittleAudioPlayer";
	private static Global instance;
	
	public static List<String> path = new ArrayList<>();
	
	public static final Action action = new Action();
	
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
		
		// Load the Resource Tree
		ResTree.init(this);
		
		// Connect to the Service AudioPlayer
		SessionToken sessionToken = new SessionToken(this,
				new ComponentName(this, AudioPlayer.class));
		
		ListenableFuture<MediaController> controllerFuture =
				new MediaController.Builder(this, sessionToken).buildAsync();
		
		controllerFuture.addListener(() -> {
			try {
				// Now the controller is connected to your AudioPlayer service
				mediaController = controllerFuture.get();
				mediaController.addListener(pb_listener);
			} catch (Exception ex) {
				Log.e(APP_TAG, "Fatal: " + ex);
				System.exit(1);
			}
		}, MoreExecutors.directExecutor());
		
		// Welcome the User
		Log.i(APP_TAG, "Welcome to my LittleAudioPlayer");
		Log.d(APP_TAG, ResTree.library.toString());
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
		if (exoState == Player.STATE_IDLE || exoState == Player.STATE_ENDED) {
			return PlayBackState.NONE;
		} else if (exoState == Player.STATE_READY || exoState == Player.STATE_BUFFERING) {
			return playWhenReady ? PlayBackState.PLAYING : PlayBackState.PAUSED;
		} else {
			return PlayBackState.NONE;
		}
	}
	
	//region Path logic
	public static void enterSubfolder(@NonNull Directory subfolder) {
		path.add(subfolder.getName());
		ResTree.current_folder = ResTree.load_folder(path);
		Signals.emitSignal("onPathChanged");
	}
	
	public static void leaveSubfolder() {
		path.remove(path.size() - 1);
		ResTree.current_folder = ResTree.load_folder(path);
		Signals.emitSignal("onPathChanged");
	}
	//endregion
	//region AudioLogic
	public static void setAudio(@NonNull Music audio, boolean playImmediately) {
		Global.current_audio = audio;
		mediaController.setMediaItem(
				new MediaItem.Builder().setUri(audio.getAbspath()).build()
		);
		mediaController.prepare();
		
		if (playImmediately) {
			mediaController.play();
		}
		Signals.emitSignal("onAudioSet");
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
	
	public static void seekTo(int seconds) {
		mediaController.seekTo(seconds * 1000L);
	}
	//endregion
}
