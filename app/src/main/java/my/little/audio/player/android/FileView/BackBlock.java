package my.little.audio.player.android.FileView;

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.LinearLayout;

import my.little.audio.player.android.R;
import my.little.audio.player.android.Global;

public class BackBlock extends LinearLayout {
	public BackBlock(Context context) {
		super(context);
		init(context);
	}
	
	public BackBlock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.back_block_layout, this, true);
		// Onclick
		findViewById(R.id.container).setOnClickListener(view -> Global.leaveSubfolder());
	}
}
