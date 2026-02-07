package my.little.audio.player.android.Prefrences;

// This file is part of 'my.little.audio.player.android'
// It is published on github under the LGPL License:
// https://github.com/lomjek/my.little.audio.player.android

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import my.little.audio.player.android.R;
import my.little.audio.player.android.Signals;

public class Preferences extends LinearLayout {
	View backArrow;
	
	public Preferences(Context context) {
		super(context);
		init(context);
	}
	
	public Preferences(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.preferences_layout, this, true);
		backArrow = findViewById(R.id.preferences_back_block);
		backArrow.setOnClickListener(view -> Signals.emitSignal("onTogglePreferences"));
	}
}
