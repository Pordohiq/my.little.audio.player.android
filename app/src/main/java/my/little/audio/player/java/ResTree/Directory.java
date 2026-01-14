package my.little.audio.player.java.ResTree;

import java.util.List;

public class Directory extends DiskElement {
	private List<DiskElement> children;
	
	public Directory (String new_abspath) {
		super(new_abspath);
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
	
	@Override
	public String toString() {
		return "Directory: " + name + " (" + children.size() + " children)";
	}
}
