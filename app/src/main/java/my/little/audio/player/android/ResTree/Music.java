package my.little.audio.player.android.ResTree;

import android.net.Uri;

import androidx.annotation.NonNull;

public class Music extends DiskElement {
	private String extension;
	
	public Music (String new_name, @NonNull Uri new_uri, String new_extension) {
		super(new_name, new_uri);
		extension = new_extension;
	}
	
	@Override
	public String toString() {
		return "Music: " + name + " (" + extension + ")";
	}
}
