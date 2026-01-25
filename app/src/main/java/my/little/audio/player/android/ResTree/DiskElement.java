package my.little.audio.player.android.ResTree;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

import java.util.List;

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
