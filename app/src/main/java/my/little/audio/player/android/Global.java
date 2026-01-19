package my.little.audio.player.android;

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
	
	public enum PlayBackState {
		PLAYING,
		PAUSED,
		NONE
	}
	public static PlayBackState current_playbackState = PlayBackState.NONE;
	
	public static Music current_audio;
	
	private static MediaController mediaController;
	private static final Player.Listener pb_listener  = new Player.Listener() {
		@Override
		public void onPlaybackStateChanged(int playbackState) {
			Player.Listener.super.onPlaybackStateChanged(playbackState);
			current_playbackState = mapCustomPBState(playbackState, mediaController.getPlayWhenReady());
			Signals.emitSignal(Signals.SignalType.PB_STATE_CHANGED);
		}
		
		@Override
		public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
			Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
			current_playbackState = mapCustomPBState(mediaController.getPlaybackState(), playWhenReady);
			Signals.emitSignal(Signals.SignalType.PB_STATE_CHANGED);
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
		
		// Load the Resource Tree
		ResTree.init("/storage/emulated/0/Music");
		
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
			} catch (Exception e) {
				e.printStackTrace();
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
	
	private static PlayBackState mapCustomPBState(int exoState, boolean playWhenReady) {
		if (exoState == Player.STATE_IDLE || exoState == Player.STATE_ENDED) {
			return PlayBackState.NONE;
		} else if (exoState == Player.STATE_READY || exoState == Player.STATE_BUFFERING) {
			return playWhenReady ? PlayBackState.PLAYING : PlayBackState.PAUSED;
		} else {
			return PlayBackState.NONE;
		}
	}
	
	public static Global getInstance() {
		return instance;
	}
	
	//region Path logic
	public static void enterSubfolder(@NonNull Directory subfolder) {
		path.add(subfolder.getName());
		ResTree.current_folder = ResTree.load_folder(path);
		Signals.emitSignal(Signals.SignalType.PATH_CHANGED);
	}
	
	public static void leaveSubfolder() {
		path.remove(path.size() - 1);
		ResTree.current_folder = ResTree.load_folder(path);
		Signals.emitSignal(Signals.SignalType.PATH_CHANGED);
	}
	//endregion
	//region AudioLogic
	public static void setAudio(@NonNull Music audio, boolean playImmediately) {
		Global.current_audio = audio;
		Signals.emitSignal(Signals.SignalType.AUDIO_SET);
		if (playImmediately) {
			mediaController.setMediaItem(
					new MediaItem.Builder().setUri(audio.getAbspath()).build()
			);
			mediaController.prepare();
			mediaController.play();
		}
	}
	
	public static void togglePlayback(){
		if (Global.current_playbackState == Global.PlayBackState.PLAYING) {
			mediaController.pause();
		}
		else if (Global.current_playbackState == Global.PlayBackState.PAUSED) {
			mediaController.play();
		}
		else {
			mediaController.play();
		}
	}
	//endregion
}
