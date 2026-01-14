package my.little.audio.player.java.ResTree;

import java.util.List;

abstract public class DiskElement {
	protected String name;
	private String abspath;
	
	public DiskElement (String new_abspath) {
		move(new_abspath);
	}
	
	public void move(String new_abspath) {
		name = new_abspath.substring(new_abspath.lastIndexOf('/') + 1);
		abspath = new_abspath;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAbspath() {
		return abspath;
	}
}
