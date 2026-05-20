package my.little.audio.player.android.queues;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 Licence:
// https://github.com/Pordohiq/my.little.audio.player.android

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import my.little.audio.player.android.ResTree.ResTree;

public class FolderQueue extends Queue {
	protected boolean recursive;
	protected List<String> path;
	
	public FolderQueue(@NonNull List<String> path, boolean recursive){
		super("__folder_queue__", ResTree.get_musics_at_path(new ArrayList<>(path), recursive));
		this.recursive = recursive;
		this.path = new ArrayList<>(path);
	}
	
	@NonNull
	public List<String> get_path() { return this.path; }
	
	public boolean is_recursive() { return this.recursive; }
}
