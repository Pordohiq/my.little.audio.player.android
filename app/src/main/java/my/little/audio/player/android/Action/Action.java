package my.little.audio.player.android.Action;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.queues.Queue;
import my.little.audio.player.android.queues.Queues;

public class Action {
	//region lockState
	public enum LockState {
		NONE,
		AUDIO,
		FOLDER,
		DISK_ELEMENT,
		QUEUE,
		ALL
	}
	private static LockState lockState = LockState.NONE;
	public static LockState get_lockState() { return lockState; }
	public static void set_lockState(LockState state) {
		lockState = state;
		Signals.emitSignal("onLockStateChanged");
	}
	//endregion
	public Action() {
		Signals.createEvent("onActionElementChanged");
		Signals.createEvent("onActionQueueChanged");
		
		Signals.createEvent("onLockStateChanged");
		Signals.createEvent("requestLibRootPathFromSysDialog");
		
		Signals.createEvent("requestShowInfoAudio");
		Signals.createEvent("requestShowInfoDir");

		Signals.subscribeToEvent("onPathChanged", Action::unset_element);
	}
	
	//region DiskElement
	@Nullable
	private static DiskElement currentElement = null;
	
	public static void unset_element() {
		Log.i(Global.APP_TAG, "Unsetting element");
		currentElement = null;
		Signals.emitSignal("onActionElementChanged");
	}
	
	public static DiskElement get_element() { return currentElement; }
	
	public static void set_element(@NonNull DiskElement element) {
		Log.i(Global.APP_TAG, "Setting element: " + element.getName());
		if (element == currentElement) {
			unset_element();
		} else {
			currentElement = element;
		}
		Signals.emitSignal("onActionElementChanged");
	}
	//endregion
	//region Queue
	@Nullable
	private static Queue currentQueue = null;
	
	public static Queue get_queue() { return currentQueue; }
	
	public static void unset_queue(){
		Log.i(Global.APP_TAG, "Unsetting queue");
		currentQueue = null;
		Signals.emitSignal("onActionQueueChanged");
	}
	
	public static void set_queue(@NonNull Queue queue){
		Log.i(Global.APP_TAG, "Setting queue: " + queue.get_name());
		if (queue == currentQueue) {
			unset_queue();
		} else {
			currentQueue = queue;
		}
		Signals.emitSignal("onActionQueueChanged");
	}
	//endregion
	private static void system_string_dialog(@NonNull Context ActivityContext, @NonNull String title, @NonNull Consumer<String> callback, @Nullable String input_text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ActivityContext);
		builder.setTitle(title);

		final EditText input = new EditText(ActivityContext);
		if (input_text != null) {
			input.setText(input_text);
		}

		FrameLayout container = new FrameLayout(ActivityContext);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT
		);

		params.setMargins(48, 20, 48, 20);
		input.setLayoutParams(params);
		container.addView(input);

		builder.setView(container);

		builder.setPositiveButton(R.string.global_accept, (dialog, which) -> {
			String result = input.getText().toString();
			Log.i("APP_TAG", "The user entered: " + result);
			callback.accept(result);
		});

		builder.setNegativeButton(R.string.global_cancel, (dialog, which) -> dialog.cancel());

		builder.show();
	}

	//region ActionBar_general functions
	public static void button_refresh(Activity context) {
		set_lockState(Action.LockState.ALL);
		Global.executor.execute(() -> {
			Log.i(Global.APP_TAG, "Refreshing library");
			ResTree.reload_from_disk(Global.getInstance());
			context.runOnUiThread(() -> {
				Global.setPath(new ArrayList<>());
				Signals.emitSignal("onPathChanged");
				set_lockState(Action.LockState.NONE);
			});
		});
	}
	
	public static void open_new_lib_root_dialog(){
		Log.i(Global.APP_TAG, "Opening new library root dialog");
		Signals.emitSignal("requestLibRootPathFromSysDialog");
	}
	
	public static void set_new_lib_root_path(Uri path){
		Log.i(Global.APP_TAG, "Setting new library root path: " + path);
		Global.setPath(new ArrayList<>());
		ResTree.set_library_path(path);
		ResTree.reload_from_disk(Global.getInstance());
		Signals.emitSignal("onPathChanged");
	}
	
	public static void open_new_audio_file_dialog(){
		Log.i(Global.APP_TAG, "Opening new audio file dialog");
		Signals.emitSignal("requestAudioFromSysDialog");
	}
	
	public static void import_music(Uri path) {
		ResTree.add_audio_file(path, Global.getPath());
	}
	
	public static void request_system_folder_name(Context ActivityContext) {
		system_string_dialog(ActivityContext, Global.getInstance().getString(R.string.action_popup_new_folder), Action::create_new_folder_at_path, null);
	}
	
	public static void request_system_queue_name(Context ActivityContext){
		system_string_dialog(ActivityContext, Global.getInstance().getString(R.string.action_popup_new_queue), Queues::create_new_queue, null);
	}
	
	private static void create_new_folder_at_path(String folderName) {
		ResTree.create_new_folder(Global.getPath(), folderName);
		Signals.emitSignal("onPathChanged");
	}
	
	public static void reset_app_data(){
		ActivityManager am = (ActivityManager) Global.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
		if (am != null) {
			am.clearApplicationUserData();
		}
	}
	//endregion
	//region ActionBar_element functions
	public static void rename_element(Context context) {
		if (currentElement == null) return;
		Log.i(Global.APP_TAG, "Renaming element: " + currentElement.getName());
		system_string_dialog(context, context.getString(R.string.action_element_rename_sysDialog), Action::finish_element_renaming, currentElement.getName());
	}

	private static void finish_element_renaming(@NonNull String new_name) {
		ResTree.rename_file(currentElement, new_name);
		Signals.emitSignal("onPathChanged");
		unset_element();
	}

	public static void move_file(@NonNull DiskElement element, @NonNull List<String> path) {
		Log.i(Global.APP_TAG, "Moving file " + element.getName() + " to new path: " + String.join("/", path));
		ResTree.move_file(element, path);
		unset_element();
		Signals.emitSignal("onPathChanged");
	}

	public static void delete_element() {
		Log.i(Global.APP_TAG, "Deleting element");
		ResTree.delete_file(currentElement);
		unset_element();
		Signals.emitSignal("onPathChanged");
	}
	//endregion
	//region ActionBar_queue functions
	public static void rename_queue(Context context) {
		if (currentQueue == null) return;
		system_string_dialog(context, context.getString(R.string.action_queue_rename_sysDialog), Action::finish_queue_renaming, currentQueue.get_name());
	}
	
	private static void finish_queue_renaming(@NonNull String new_name) {
		if (currentQueue == null) return;
		Queues.rename_queue(currentQueue, new_name);
		unset_queue();
	}
	
	public static void trash_queue(){
		if (currentQueue == null) return;
		Queues.loaded_queues.remove(currentQueue);
		Queues.save_queues();
		Signals.emitSignal("onQueueLibChanged");
		unset_queue();
	}
	//endregion
	//region info_audio
	public static long getFileSize(@NonNull Context context, Uri uri) {
		long fileSize = -1;
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		
		if (cursor != null && cursor.moveToFirst()) {
			int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
			if (!cursor.isNull(sizeIndex)) {
				fileSize = cursor.getLong(sizeIndex);
			}
			cursor.close();
		}
		return fileSize;
	}
	
	public static int getChannelCount(Uri uri) {
		if (uri == null) return -1;
		
		MediaExtractor extractor = new MediaExtractor();
		try {
			extractor.setDataSource(Global.getInstance(), uri, null);
			int trackCount = extractor.getTrackCount();
			
			for (int i = 0; i < trackCount; i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				
				if (mime != null && mime.startsWith("audio/")) {
					if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
						return format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
					}
				}
			}
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error: " + e);
		} finally {
			extractor.release();
		}
		return -1;
	}
	
	public static int getSampleRate(Uri uri) {
		if (uri == null) return -1;
		
		MediaExtractor extractor = new MediaExtractor();
		try {
			extractor.setDataSource(Global.getInstance(), uri, null);
			int trackCount = extractor.getTrackCount();
			
			for (int i = 0; i < trackCount; i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				
				if (mime != null && mime.startsWith("audio/")) {
					if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
						return format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
					}
				}
			}
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error: " + e);
		} finally {
			extractor.release();
		}
		return -1;
	}
	public static int getBitDepth(Uri uri) {
		if (uri == null) return -1;
		
		MediaExtractor extractor = new MediaExtractor();
		try {
			extractor.setDataSource(Global.getInstance(), uri, null);
			int trackCount = extractor.getTrackCount();
			
			for (int i = 0; i < trackCount; i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				
				if (mime != null && mime.startsWith("audio/")) {
					if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
						int encoding = format.getInteger(MediaFormat.KEY_PCM_ENCODING);
						
						switch (encoding) {
							case AudioFormat.ENCODING_PCM_8BIT:
								return 8;
							case AudioFormat.ENCODING_PCM_24BIT_PACKED:
							case AudioFormat.ENCODING_PCM_FLOAT:
								return 24;
							case AudioFormat.ENCODING_PCM_32BIT:
								return 32;
							case AudioFormat.ENCODING_PCM_16BIT:
							default:
								return 16;
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error: " + e);
		} finally {
			extractor.release();
		}
		return -1;
	}
	
	@NonNull
	public static String getMimeType(@NonNull Uri uri) {
		MediaExtractor extractor = new MediaExtractor();
		try {
			extractor.setDataSource(Global.getInstance(), uri, null);
			
			// Loop through tracks to find the audio track
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				
				if (mime != null && mime.startsWith("audio/")) {
					return mime; // Returns e.g., "audio/mpeg", "audio/opus"
				}
			}
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error: " + e);
		} finally {
			extractor.release();
		}
		return "unknown";
	}
	
	@Nullable
	public static HashMap<String, Object> get_audio_info(){
		if (currentElement == null) return null;
		if (!(currentElement instanceof Music)) return null;
		return get_audio_info((Music) currentElement);
	}
	
	@NonNull
	public static HashMap<String, Object> get_audio_info(@NonNull Music audio) {
		HashMap<String, Object> info = new HashMap<>();
		
		try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
			retriever.setDataSource(Global.getInstance(), audio.getUri());
			
			info.put("title", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
			info.put("artist", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
			info.put("album", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
			info.put("year", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR));
			info.put("bitrate", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
			info.put("channels", getChannelCount(audio.getUri()));
			info.put("sample_rate", getSampleRate(audio.getUri()));
			info.put("bit_depth", getBitDepth(audio.getUri()));
			info.put("encoding", getMimeType(audio.getUri()));
			
			// File size still comes from your helper
			info.put("file_size", getFileSize(Global.getInstance(), audio.getUri()));
			
		} catch (IOException ex) {
			Log.e(Global.APP_TAG, "Error making the Metadata retriever: " + ex);
		} catch (Exception e) {
			Log.e(Global.APP_TAG, "Error: " + e);
		}
		
		return info;
	}
}
