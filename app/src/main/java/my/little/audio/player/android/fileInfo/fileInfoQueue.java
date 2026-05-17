package my.little.audio.player.android.fileInfo;

import static my.little.audio.player.android.fileInfo.fileInfoTools.formatDuration;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.IOException;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.Signals;
import my.little.audio.player.android.queues.Queue;
import my.little.audio.player.android.ResTree.Music;

public class fileInfoQueue extends LinearLayout {
	private Activity activity;
	private long queue_duration;
	private TextView length;
	private TextView elements;
	
	public fileInfoQueue(Context context) {
		super(context);
		init(context);
	}
	
	public fileInfoQueue(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		activity = (Activity) getContext();
		
		LayoutInflater.from(context).inflate(R.layout.info_queue, this, true);
		
		length = findViewById(R.id.info_queue_length);
		elements = findViewById(R.id.info_queue_element_count);
		
		hide();
		Signals.subscribeToEvent("requestShowInfoQueue", this::load_data);
		
		Signals.subscribeToEvent("onPathChanged", this::hide);
		Signals.subscribeToEvent("onActionElementChanged", this::hide);
		Signals.subscribeToEvent("onActionQueueChanged", this::hide);
		Signals.subscribeToEvent("onDisplayStateChanged", this::hide);
	}
	
	private void load_data(){
		if (this.getVisibility() == VISIBLE){
			hide();
			queue_duration = 0L;
			return;
		}
		
		Queue que = Action.get_queue();
		if (que == null){
			hide();
			queue_duration = 0L;
			return;
		}
		
		String element_count = activity.getString(R.string.info_queue_element_count) + que.get_song_count();
		elements.setText(element_count);
		
		for (Music msc : que.get_songs()) {
			Global.executor.execute(() -> {
				try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()){
					retriever.setDataSource(activity, msc.getUri());
					String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
					assert durationStr != null;
					long durationMs = Long.parseLong(durationStr);
					long durationSeconds = durationMs / 1000;
					queue_duration += durationSeconds;
					activity.runOnUiThread(this::update_duration_widget);
				} catch (IOException ioex) {
					Log.e(Global.APP_TAG, "Could not calculate the length of the Audio. IOException");
					hide();
				} catch (Exception ex){
					Log.e(Global.APP_TAG, "Error calculating length of Audio.");
					hide();
				}
			});
		}
		
		this.setVisibility(VISIBLE);
	}
	
	private void update_duration_widget(){
		String str = activity.getString(R.string.info_queue_length) + formatDuration(queue_duration);
		length.setText(str);
	}
	
	private void hide(){
		this.setVisibility(GONE);
	}
}
