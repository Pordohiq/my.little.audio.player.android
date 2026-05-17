package my.little.audio.player.android.fileInfo;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.Global;
import my.little.audio.player.android.R;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.ResTree.ResTree;
import my.little.audio.player.android.Signals;

public class fileInfoAudio extends LinearLayout {
	private TextView info_audio_title;
	private TextView info_audio_bitrate;
	private TextView info_audio_artist;
	private TextView info_audio_bit_depth;
	private TextView info_audio_album;
	private TextView info_audio_sample_rate;
	private TextView info_audio_year;
	private TextView info_audio_channels;
	private TextView info_audio_file_size;
	
	private Activity activity;
	
	public fileInfoAudio(Context context) {
		super(context);
		init(context);
	}
	
	public fileInfoAudio(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.info_audio, this, true);
		info_audio_title = findViewById(R.id.info_audio_title);
		info_audio_bitrate = findViewById(R.id.info_audio_bitrate);
		info_audio_artist = findViewById(R.id.info_audio_artist);
		info_audio_bit_depth = findViewById(R.id.info_audio_bit_depth);
		info_audio_album = findViewById(R.id.info_audio_album);
		info_audio_sample_rate = findViewById(R.id.info_audio_sample_rate);
		info_audio_year = findViewById(R.id.info_audio_year);
		info_audio_channels = findViewById(R.id.info_audio_channels);
		info_audio_file_size = findViewById(R.id.info_audio_file_size);
		hide();
		
		this.activity = (Activity) getContext();
		
		Signals.subscribeToEvent("requestShowInfoAudio", this::load_data);
		
		Signals.subscribeToEvent("onPathChanged", this::hide);
		Signals.subscribeToEvent("onActionElementChanged", this::hide);
		Signals.subscribeToEvent("onActionQueueChanged", this::hide);
		Signals.subscribeToEvent("onDisplayStateChanged", this::hide);
	}
	
	//region FETCH_AUDIO_INFO
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
	//endregion
	
	public void load_data(){
		if (this.getVisibility() == VISIBLE){
			hide();
			return;
		}
		
		this.removeAllViews();
		LayoutInflater.from(getContext()).inflate(R.layout.info_audio, this, true);
		
		info_audio_title = findViewById(R.id.info_audio_title);
		info_audio_bitrate = findViewById(R.id.info_audio_bitrate);
		info_audio_artist = findViewById(R.id.info_audio_artist);
		info_audio_bit_depth = findViewById(R.id.info_audio_bit_depth);
		info_audio_album = findViewById(R.id.info_audio_album);
		info_audio_sample_rate = findViewById(R.id.info_audio_sample_rate);
		info_audio_year = findViewById(R.id.info_audio_year);
		info_audio_channels = findViewById(R.id.info_audio_channels);
		info_audio_file_size = findViewById(R.id.info_audio_file_size);
		
		this.setVisibility(VISIBLE);
		
		HashMap<String, Object> info = Action.get_audio_info();
		
		if (info == null) return;
		
		String title = (String) info.get("title");
		if (title == null) title = "-";
		
		String bitrateStr = (String) info.get("bitrate");
		int bitrate = 0;
		if (bitrateStr != null) {
			try {
				bitrate = Integer.parseInt(bitrateStr);
			} catch (NumberFormatException e) {
				Log.e(Global.APP_TAG, "Couldn't parse int.");
			}
		}
		bitrate /= 1000;
		
		String artist = (String) info.get("artist");
		if (artist == null) artist = "-";
		
		String album = (String) info.get("album");
		if (album == null) album = "-";
		
		Integer year = (Integer) info.get("year");
		if (year == null) year = 0;
		
		String title_name = getContext().getString(R.string.info_audio_title) + title;
		info_audio_title.setText(title_name);
		
		String bit_rate = getContext().getString(R.string.info_audio_bitrate) + bitrate + " kb/s";
		info_audio_bitrate.setText(bit_rate);
		
		String artist_name = getContext().getString(R.string.info_audio_artist) + artist;
		info_audio_artist.setText(artist_name);
		
		String album_name = getContext().getString(R.string.info_audio_album) + album;
		info_audio_album.setText(album_name);
		
		String year_name = getContext().getString(R.string.info_audio_year) + year;
		info_audio_year.setText(year_name);
		
		Global.executor.execute(() -> {
			if (Action.get_element() == null || !(Action.get_element() instanceof Music)) return;
			int bit_depth = getBitDepth(Action.get_element().getUri());
			String encoding = getMimeType(Action.get_element().getUri());
			
			if (bit_depth >= 0) {
				String bd = getContext().getString(R.string.info_audio_bit_depth) + bit_depth + " b";
				activity.runOnUiThread(() -> info_audio_bit_depth.setText(bd));
			} else {
				String codec = getContext().getString(R.string.info_audio_codec) + encoding;
				activity.runOnUiThread(() -> info_audio_bit_depth.setText(codec));
			}
		});
		
		Global.executor.execute(() -> {
			if (Action.get_element() == null || !(Action.get_element() instanceof Music)) return;
			int sample_rate = getSampleRate(Action.get_element().getUri());
			String sr = getContext().getString(R.string.info_audio_sample_rate) + sample_rate + " Hz";
			activity.runOnUiThread(() ->  info_audio_sample_rate.setText(sr));
		});
		
		Global.executor.execute(() -> {
			if (Action.get_element() == null || !(Action.get_element() instanceof Music)) return;
			int channels = getChannelCount(Action.get_element().getUri());
			String channels_str = getContext().getString(R.string.info_audio_channels) + channels;
			activity.runOnUiThread(() -> info_audio_channels.setText(channels_str));
		});
		
		Global.executor.execute(() -> {
			if (Action.get_element() == null || !(Action.get_element() instanceof Music)) return;
			long file_size = ResTree.getFileSize(activity, Action.get_element().getUri());
			String fs = getContext().getString(R.string.info_audio_file_size) + fileInfoTools.formatFileSize(file_size);
			activity.runOnUiThread(() -> info_audio_file_size.setText(fs));
		});
	}
	
	private void hide(){
		this.setVisibility(GONE);
	}
}
