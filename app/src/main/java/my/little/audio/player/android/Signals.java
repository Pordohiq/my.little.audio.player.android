package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Signals {
	private static final Map<String, List<Runnable>> events = new HashMap<>();
	
	public static void createEvent(@NonNull String eventName) {
		if (!events.containsKey(eventName)) {
			events.put(eventName, new ArrayList<>());
		}
	}
	
	private static void cleanUp(@NonNull String eventName) {
		List<Runnable> callbacks = events.get(eventName);
		if (callbacks != null) {
			callbacks.removeIf(Objects::isNull);
		}
	}
	
	public static void subscribeToEvent(@NonNull String eventName, Runnable callback) {
		if (!events.containsKey(eventName)) {
			createEvent(eventName);
		}
		Objects.requireNonNull(events.get(eventName)).add(callback);
	}
	
	public static void emitSignal(@NonNull String eventName) {
		Log.d("Global.APP_TAG", "Emitting signal: " + eventName);
		List<Runnable> callbacks = events.get(eventName);
		if (callbacks != null) {
			cleanUp(eventName);
			for (Runnable callback : new ArrayList<>(callbacks)) {
				callback.run();
			}
		}
	}
}