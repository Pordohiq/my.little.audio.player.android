package my.little.audio.player.android.ResTree;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import android.net.Uri;

import androidx.annotation.NonNull;

public class Music extends DiskElement {
	private final String extension;
	
	public Music (String new_name, @NonNull Uri new_uri, String new_extension) {
		super(new_name, new_uri);
		extension = new_extension;
	}
	
	@Override @NonNull
	public String toString() {
		return "Music: " + name + " (" + extension + ")";
	}
}
