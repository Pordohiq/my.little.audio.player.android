package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import my.little.audio.player.android.Action.Action;

public class Preferences extends LinearLayout {
	private Context ct;
	
	public Preferences(Context context) {
		super(context);
		init(context);
	}
	
	public Preferences(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		ct = context;
		LayoutInflater.from(context).inflate(R.layout.preferences_layout, this, true);
		
		findViewById(R.id.preferences_back_block).setOnClickListener(view -> Signals.emitSignal("onTogglePreferences"));
		
		findViewById(R.id.preferences_action_refresh).setOnClickListener(view -> Action.button_refresh((Activity) getContext()));
		findViewById(R.id.preferences_action_set_library).setOnClickListener(view -> Action.open_new_lib_root_dialog());
		findViewById(R.id.preferences_reset_app).setOnClickListener(view -> Action.reset_app_data());

		findViewById(R.id.preferences_github_block).setOnClickListener(view -> open_link("https://github.com/Pordohiq/my.little.audio.player.android"));
		findViewById(R.id.preferences_youtube_block).setOnClickListener(view -> open_link("https://www.youtube.com/@pordohiq9000"));
		findViewById(R.id.preferences_license_block).setOnClickListener(view -> open_link("https://www.gnu.org/licenses/lgpl-3.0-standalone.html"));
	}
	
	private void open_link(String url){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		ct.startActivity(intent);
	}
}
