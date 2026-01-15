package my.little.audio.player.java;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class Signals {
	Signals() {}
	private static List<Runnable> onPathChanged = new ArrayList<>();
	
	public enum SignalType {
		PATH_CHANGED,
		PB_STATE_CHANGED
	}
	
	private static void cleanUp(SignalType type) {
		switch (type) {
			case PATH_CHANGED:
				onPathChanged.removeIf(Objects::isNull);
				break;
			case PB_STATE_CHANGED:
				return; // FIX
			default:
				throw new IllegalArgumentException("Unknown signal type: " + type);
		}
	}
	
	public static void subscribeToEvent(SignalType type, Runnable callback) {
		switch (type) {
			case PATH_CHANGED:
				onPathChanged.add(callback);
				break;
			case PB_STATE_CHANGED:
				return; // FIX
			default:
				throw new IllegalArgumentException("Unknown signal type: " + type);
		}
	}
	
	public static void unsubscribeFromEvent(SignalType type, Runnable callback) {
		switch (type) {
			case PATH_CHANGED:
				onPathChanged.remove(callback);
				break;
			case PB_STATE_CHANGED:
				return; // FIX
			default:
				throw new IllegalArgumentException("Unknown signal type: " + type);
		}
	}
	
	public static void emitSignal(SignalType type) {
		switch (type) {
			case PATH_CHANGED:
				cleanUp(SignalType.PATH_CHANGED);
				for (Runnable callback : onPathChanged) {
					callback.run();
				}
				break;
			case PB_STATE_CHANGED:
				return;
			default:
				throw new IllegalArgumentException("Unknown signal type: " + type);
		}
	}
}
