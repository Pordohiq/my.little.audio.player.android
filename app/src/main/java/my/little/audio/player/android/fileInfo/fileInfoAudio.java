package my.little.audio.player.android.fileInfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

import my.little.audio.player.android.Action.Action;
import my.little.audio.player.android.R;
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
		Signals.subscribeToEvent("requestShowInfoAudio", this::load_data);
		
		Signals.subscribeToEvent("onPathChanged", this::hide);
		Signals.subscribeToEvent("onActionElementChanged", this::hide);
		Signals.subscribeToEvent("onActionQueueChanged", this::hide);
	}
	
	public void load_data(){
		if (this.getVisibility() == VISIBLE){
			hide();
			return;
		}
		
		HashMap<String, Object> info = Action.get_audio_info();
		if (info == null) return;
		
		String title = (String) info.get("title");
		if (title == null) title = "-";
		
		String bitrateStr = (String) info.get("bitrate");
		int bitrate = 0;
		if (bitrateStr != null) {
			bitrate = Integer.parseInt(bitrateStr);
		}
		bitrate /= 1000;
		
		String artist = (String) info.get("artist");
		if (artist == null) artist = "-";
		
		Integer bit_depth = (Integer) info.get("bit_depth");
		if (bit_depth == null) bit_depth = 0;
		
		String encoding = (String) info.get("encoding");
		if (encoding == null) encoding = "-";
		
		String album = (String) info.get("album");
		if (album == null) album = "-";
		
		Integer sample_rate = (Integer) info.get("sample_rate");
		if (sample_rate == null) sample_rate = 0;
		
		Integer year = (Integer) info.get("year");
		if (year == null) year = 0;
		
		Integer channels = (Integer) info.get("channels");
		if (channels == null) channels = 0;
		
		Long file_size = (Long) info.get("file_size");
		if (file_size == null) file_size = (long) 0;
		
		display_data(title, String.valueOf(bitrate), artist, bit_depth, encoding, album, sample_rate, year, channels, file_size);
	}
	
	public void display_data(@NonNull String title, @NonNull String bitrate, @NonNull String artist, int bit_depth, @NonNull String encoding, @NonNull String album, int sample_rate, int year, int channels, Long file_size){
		this.setVisibility(VISIBLE);
		
		String title_name = getContext().getString(R.string.info_audio_title) + title;
		info_audio_title.setText(title_name);
		
		String bit_rate = getContext().getString(R.string.info_audio_bitrate) + bitrate + " kb/s";
		info_audio_bitrate.setText(bit_rate);
		
		String artist_name = getContext().getString(R.string.info_audio_artist) + artist;
		info_audio_artist.setText(artist_name);
		
		if (bit_depth >= 0) {
			String bd = getContext().getString(R.string.info_audio_bit_depth) + bit_depth + " b";
			info_audio_bit_depth.setText(bd);
		} else {
			String codec = getContext().getString(R.string.info_audio_codec) + encoding;
			info_audio_bit_depth.setText(codec);
		}
		
		String album_name = getContext().getString(R.string.info_audio_album) + album;
		info_audio_album.setText(album_name);
		
		String sr = getContext().getString(R.string.info_audio_sample_rate) + sample_rate + " Hz";
		info_audio_sample_rate.setText(sr);
		
		String year_name = getContext().getString(R.string.info_audio_year) + year;
		info_audio_year.setText(year_name);
		
		String channels_str = getContext().getString(R.string.info_audio_channels) + channels;
		info_audio_channels.setText(channels_str);
		
		String fs = getContext().getString(R.string.info_audio_file_size) + file_size + " B";
		info_audio_file_size.setText(fs);
	}
	
	private void hide(){
		this.setVisibility(GONE);
	}
}
