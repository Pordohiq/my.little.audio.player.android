package my.little.audio.player.android;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class Signals {
	Signals() {}
	private static List<Runnable> onPathChanged = new ArrayList<>();
	private static List<Runnable> onPlaybackStateChanged = new ArrayList<>();
	private static List<Runnable> onAudioSet = new ArrayList<>();
	
	public enum SignalType {
		PATH_CHANGED,
		PB_STATE_CHANGED,
		AUDIO_SET,
	}
	
	private static void cleanUp(@NonNull SignalType type) {
		switch (type) {
			case PATH_CHANGED:
				onPathChanged.removeIf(Objects::isNull);
				break;
			case PB_STATE_CHANGED:
				onPlaybackStateChanged.removeIf(Objects::isNull);
				break;
			case AUDIO_SET:
				onAudioSet.removeIf(Objects::isNull);
				break;
			default:
				throw new IllegalArgumentException("Unknown signal type: " + type);
		}
	}
	
	public static void subscribeToEvent(@NonNull SignalType type, Runnable callback) {
		switch (type) {
			case PATH_CHANGED:
				onPathChanged.add(callback);
				break;
			case PB_STATE_CHANGED:
				onPlaybackStateChanged.add(callback);
				break;
			case AUDIO_SET:
				onAudioSet.add(callback);
				break;
			default:
				throw new IllegalArgumentException("Unknown signal type: " + type);
		}
	}
	
	public static void unsubscribeFromEvent(@NonNull SignalType type, Runnable callback) {
		switch (type) {
			case PATH_CHANGED:
				onPathChanged.remove(callback);
				break;
			case PB_STATE_CHANGED:
				onPlaybackStateChanged.remove(callback);
				break;
			case AUDIO_SET:
				onAudioSet.remove(callback);
				break;
			default:
				throw new IllegalArgumentException("Unknown signal type: " + type);
		}
	}
	
	public static void emitSignal(@NonNull SignalType type) {
		Log.d(Global.APP_TAG, "Emitting signal: " + type.toString());
		switch (type) {
			case PATH_CHANGED:
				cleanUp(SignalType.PATH_CHANGED);
				for (Runnable callback : onPathChanged) {
					callback.run();
				}
				break;
			case PB_STATE_CHANGED:
				for (Runnable callback : onPlaybackStateChanged) {
					callback.run();
				}
				break;
			case AUDIO_SET:
				for (Runnable callback : onAudioSet) {
					callback.run();
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown signal type: " + type);
		}
	}
}
