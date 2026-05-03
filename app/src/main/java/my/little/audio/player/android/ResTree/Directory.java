package my.little.audio.player.android.ResTree;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Directory extends DiskElement {
	private List<DiskElement> children;
	
	public Directory (String new_name, @NonNull Uri new_uri) {
		super(new_name, new_uri);
		children = new ArrayList<>();
	}
	
	public int getChildCount () {
		return children.size();
	}
	
	public DiskElement getChild(int index) {
		return children.get(index);
	}
	
	public void addChild(DiskElement child) {
		children.add(child);
	}
	
	public void setChildren(List<DiskElement> new_children) {
		children = new_children;
	}
	
	public List<DiskElement> getChildren() {
		return children;
	}
	
	@NonNull @Override
	public String toString() {
		return "Directory: " + name + " (" + children.size() + " children)";
	}

	public void removeChild(DiskElement element) {
        children.remove(element);
	}
}
