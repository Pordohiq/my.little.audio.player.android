package my.little.audio.player.android.ResTree;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Objects;

import my.little.audio.player.android.Global;

public class Music extends DiskElement {
	private final String extension;
	
	public Music (String new_name, @NonNull Uri new_uri, String new_extension) {
		super(new_name, new_uri);
		extension = new_extension;
		if (Global.current_audio == null) return;
		if (Objects.equals(new_uri.getPath(), Global.current_audio.getUri().getPath())){
			Global.current_audio = this;
		}
	}
	
	@Override @NonNull
	public String toString() {
		return "Music: " + name + " (" + extension + ")";
	}
}
