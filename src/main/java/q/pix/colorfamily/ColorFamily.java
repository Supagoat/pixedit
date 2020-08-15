package q.pix.colorfamily;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ColorFamily {
	private List<Set<Color>> colorGroups;
	private String familyName;
	public static final int FAMILY_GROUP_COUNT = 8;
	
	public ColorFamily(List<Set<Color>> colorGroups) {
		this.colorGroups = colorGroups;
	}
	
	public int size() {
		return getColorGroups().size();
	}
	
	public Set<Color> get(int index) {
		return getColorGroups().get(index);
	}
	
	// When loading from a file don't add empty indexes
	public ColorFamily(String familyName) {
		this.colorGroups = new ArrayList<>();
		this.familyName = familyName;
	}
	
	public ColorFamily() {
		setColorGroups(new ArrayList<>());
		for(int i=0;i<FAMILY_GROUP_COUNT;i++) {
			getColorGroups().add(new HashSet<>());
		}
	}
	
	public void addGroup(Set<Color> colors) {
		getColorGroups().add(colors);
	}
	
	public boolean isInFamily(Color c) {
		boolean isIn =  getColorGroup(c) > -1;
		return isIn;
	}
	
	public boolean containsAllColors(Set<Color> colors) {
		return colors.stream().allMatch(c -> isInFamily(c));
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
	
	public Color offsetLuminance(Color baseColor, Color inputColor) {
		return new SimilarColors(baseColor, inputColor).getLumOffsetColor();
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}



	private static class ColorLumComparator implements Comparator<Color> {

		@Override
		public int compare(Color o1, Color o2) {
			return new SimilarColors(o1, o2).getBrighterColor();
		}
		
	}
}