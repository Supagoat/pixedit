package q.pix.colorfamily;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ColorFamily {
	private List<Set<Color>> colorGroups;
	
	public ColorFamily(List<Set<Color>> colorGroups) {
		this.colorGroups = colorGroups;
	}
	
	public int size() {
		return getColorGroups().size();
	}
	
	public Set<Color> get(int index) {
		return getColorGroups().get(index);
	}
	
	public ColorFamily() {
		setColorGroups(new ArrayList<>());
	}
	
	public void addGroup(Set<Color> colors) {
		getColorGroups().add(colors);
	}
	
	public boolean isInFamily(Color c) {
		return getColorGroup(c) > -1;
		
	}
	
	public int getColorGroup(Color c) {
		for (int i=0;i<getColorGroups().size();i++) {
			if (getColorGroups().get(i).contains(c)) {
				return i;
			}
		}
		return  -1;
	}

	public int countFamilyColors() {
		int ct = 0;
		for(Set s : getColorGroups()) {
			ct+=s.size();
		}
		return ct;
	}
	
	public List<Set<Color>> getColorGroups() {
		return colorGroups;
	}

	public void setColorGroups(List<Set<Color>> colorGroups) {
		this.colorGroups = colorGroups;
	}
	
	// Input color is intended to be the middle color of the group as the 
	// OK I know what I want to do here but need to find the best way
	// I need to take the base color group color and the input color and
	// then map the input color to a luminance offset from the color group color
	// Wait why do I need to do this with the whole group
	// doesn't just the luminance matter?
	
	
//	public List<Color> toLuminance(Color groupColor, Color inputColor) {
//		return toLuminance(groupColor, getColorGroup(inputColor));
//	}	
//	
//	public List<Color> toLuminance(Color groupColor, int groupIndex) {
//		List<Color> groupToLum = new ArrayList<>();
//		for(Color c : get(groupIndex)) {
//			groupToLum.add(new SimilarColors(groupColor, c).getLumOffsetColor());
//		}
//		groupToLum.sort(new ColorLumComparator());
//		return groupToLum;
//	}
	
	public Color offsetLuminance(Color baseColor, Color inputColor) {
		return new SimilarColors(baseColor, inputColor).getLumOffsetColor();
	}
	 
	private static class ColorLumComparator implements Comparator<Color> {

		@Override
		public int compare(Color o1, Color o2) {
			return new SimilarColors(o1, o2).getBrighterColor();
		}
		
	}
}