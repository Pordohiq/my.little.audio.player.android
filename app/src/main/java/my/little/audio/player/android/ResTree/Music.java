package my.little.audio.player.android.ResTree;

public class Music extends DiskElement {
	private String extension;
	
	public Music (String new_abspath) {
		super(new_abspath);
		int lastDotIndex = new_abspath.lastIndexOf('.');
		if (lastDotIndex != -1) {
			extension = new_abspath.substring(lastDotIndex + 1);
		}
	}
	
	@Override
	public String toString() {
		return "Music: " + name + " (" + extension + ")";
	}
}
