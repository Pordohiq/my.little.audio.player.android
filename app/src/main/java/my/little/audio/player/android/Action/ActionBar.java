package my.little.audio.player.android.Action;

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import my.little.audio.player.android.R;

public class ActionBar extends LinearLayout {
	
	
	public ActionBar(Context context) {
		super(context);
		init(context);
	}
	
	public ActionBar(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public ActionBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	
	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.action_bar_layout, this, true);
		// Get the nodes
		ImageView add_music_button = findViewById(R.id.action_general_add_music);
		ImageView add_folder_button = findViewById(R.id.action_general_add_folder);
		ImageView refresh_button = findViewById(R.id.action_general_refresh);
		ImageView set_library_button = findViewById(R.id.action_general_set_library);
		ImageView open_settings_button = findViewById(R.id.action_general_settings);
		
		// Link the nodes
		add_music_button.setOnClickListener(view -> Action.open_new_audio_file_dialog());
		add_folder_button.setOnClickListener(view -> {
			// TODO: Hook up
		});
		refresh_button.setOnClickListener(view -> Action.button_refresh());
		set_library_button.setOnClickListener(view -> Action.open_new_lib_root_dialog());
		open_settings_button.setOnClickListener(view -> {
			// TODO: Hook up
		});
	}
}
