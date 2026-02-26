package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import android.util.Log;

public class MixingState {
	//region repeat_state
	public enum repeat_state {
		NONE,
		ONE,
		QUEUE
	}
	private repeat_state current_repeat_state = repeat_state.NONE;
	public repeat_state get_repeat_state() { return current_repeat_state; }
	public void toggle_repeat_state() {
		if (current_repeat_state == repeat_state.NONE) {
			if (current_queue_state != queue_state.NONE) {
				current_repeat_state = repeat_state.QUEUE;
			}
			else {
				current_repeat_state = repeat_state.ONE;
			}
		} else if (current_repeat_state == repeat_state.QUEUE) {
			if (current_queue_state == queue_state.NONE) {
				Log.w(Global.APP_TAG, "Theoretically impossible MX_State reached. Will set back repeat to NONE");
			}
			current_repeat_state = repeat_state.NONE;
		} else if (current_repeat_state == repeat_state.ONE) {
			current_repeat_state = repeat_state.NONE;
		} else {
			Log.w(Global.APP_TAG, "Theoretically impossible MX_State reached. Will set back repeat to NONE");
		}
		
		Signals.emitSignal("onMxStateChanged");
	}
	//endregion
	//region shuffle_state
	public enum shuffle_state {
		NONE,
		ON
	}
	private shuffle_state current_shuffle_state = shuffle_state.NONE;
	public shuffle_state get_shuffle_state() { return current_shuffle_state; }
	public void toggle_shuffle_state() {
		if (current_shuffle_state == shuffle_state.NONE) {
			current_shuffle_state = shuffle_state.ON;
			if (current_queue_state == queue_state.NONE) toggle_queue_state();
		} else if (current_shuffle_state == shuffle_state.ON) {
			current_shuffle_state = shuffle_state.NONE;
		} else {
			Log.w(Global.APP_TAG, "Theoretically impossible MX_State reached. Will set back shuffle to NONE");
			current_shuffle_state = shuffle_state.NONE;
		}
		
		Signals.emitSignal("onMxStateChanged");
	}
	//endregion
	//region queue_state
	public enum queue_state {
		NONE,
		LOADED_QUEUE,
		DIRECTORY,
		RECURSIVE_DIRECTORY
	}
	private queue_state current_queue_state = queue_state.NONE;
	public queue_state get_queue_state() { return current_queue_state; }
	public void toggle_queue_state() {
		if (current_queue_state == queue_state.NONE) {
			current_queue_state = queue_state.DIRECTORY;
			
			if (current_repeat_state == repeat_state.ONE) current_repeat_state = repeat_state.QUEUE;
		} else if (current_queue_state == queue_state.DIRECTORY) {
			current_queue_state = queue_state.RECURSIVE_DIRECTORY;
			
			if (current_repeat_state == repeat_state.ONE) {
				Log.w(Global.APP_TAG, "Theoretically impossible MX_State reached. Will set back repeat to QUEUE");
				current_repeat_state = repeat_state.QUEUE;
			}
		} else if (current_queue_state == queue_state.RECURSIVE_DIRECTORY) {
			current_queue_state = queue_state.NONE;
			if (current_repeat_state == repeat_state.QUEUE) current_repeat_state = repeat_state.ONE;
			current_shuffle_state = shuffle_state.NONE;
		} else if (current_queue_state == queue_state.LOADED_QUEUE) {
			current_queue_state = queue_state.NONE;
			if (current_repeat_state == repeat_state.QUEUE) current_repeat_state = repeat_state.ONE;
			current_shuffle_state = shuffle_state.NONE;
		}
		Signals.emitSignal("onMxStateChanged");
	}
	
	public void activate_loaded_queue(){
		current_queue_state = queue_state.LOADED_QUEUE;
		Signals.emitSignal("onMxStateChanged");
	}
	//endregion
	
	public MixingState () {
		Signals.createEvent("onMxStateChanged");
	}
}
