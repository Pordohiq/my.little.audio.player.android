package my.little.audio.player.android.ResTree;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the MIT License:
// https://github.com/lomjek/my.little.audio.player.android

import android.net.Uri;

import androidx.annotation.NonNull;

abstract public class DiskElement {
	protected String name;
	private Uri elementUri;
	
	public DiskElement (String new_name, @NonNull Uri new_uri) {
		move(new_name, new_uri);
	}

	public void move(String new_name, @NonNull Uri new_uri) {
		name = new_name;
		elementUri = new_uri;
	}
	
	public String getName() {
		return name;
	}
	
	public Uri getUri() {
		return elementUri;
	}
}
