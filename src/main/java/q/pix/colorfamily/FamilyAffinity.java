package q.pix.colorfamily;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import q.pix.util.ImageUtil;

public class FamilyAffinity implements Comparable<FamilyAffinity> {
	private int matchCount;
	private int missingInFamilyCount;
	private int inFamilyButNotInImageCount; // mostly a tie breaker
	private ColorFamily colorFamily;
	
	public FamilyAffinity(int matchCount, int missingInFamilyCount, int inFamilyButNotInImageCount, ColorFamily colorFamily) {
		this.matchCount = matchCount;
		this.missingInFamilyCount = missingInFamilyCount;
		this.inFamilyButNotInImageCount = inFamilyButNotInImageCount;
		this.colorFamily = colorFamily;
	}
	
	public FamilyAffinity (Set<Color> colors, ColorFamily colorFamily) {

		int matchCount = 0;
		int missingInFamilyCount = 0;

		for (Color c : colors) {
			if (colorFamily.isInFamily(c)) {
				matchCount++;
			} else {
				missingInFamilyCount++;
			}
		}
		int familySize = colorFamily.countFamilyColors();
		this.matchCount = matchCount;
		this.missingInFamilyCount = missingInFamilyCount;
		this.inFamilyButNotInImageCount =  familySize - matchCount;
		this.colorFamily = colorFamily;
	}

	public boolean isMatchingAffinity(Set<Color> colors) {
		return calcFamilyColorPct(colors) > 0.6;
	}
	
	public double calcFamilyColorPct(Set<Color> colors) {
		 return countColorsInFamilty(colors).doubleValue()/colors.size();
	}
	
	public Long countColorsInFamilty(Set<Color> colors) {
		return colors.stream().filter(c -> getColorFamily().isInFamily(c)).collect(Collectors.counting());
	}
	
	public List<Color> getMissingColors(Set<Color> colors) {
		return colors.stream().filter(c -> !getColorFamily().isInFamily(c)).collect(Collectors.toList());
	}
	
	
	public int getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}

	public int getMissingInFamilyCount() {
		return missingInFamilyCount;
	}

	public void setMissingInFamilyCount(int missingInFamilyCount) {
		this.missingInFamilyCount = missingInFamilyCount;
	}

	public int getInFamilyButNotInImageCount() {
		return inFamilyButNotInImageCount;
	}

	public void setInFamilyButNotInImageCount(int inFamilyButNotInImageCount) {
		this.inFamilyButNotInImageCount = inFamilyButNotInImageCount;
	}

	public ColorFamily getColorFamily() {
		return colorFamily;
	}

	public void setColorFamily(ColorFamily colorFamily) {
		this.colorFamily = colorFamily;
	}

	@Override
	public int compareTo(FamilyAffinity o) {
		if(this.getMissingInFamilyCount() < o.getMissingInFamilyCount()) {
			return -1;
		}
		if(this.getMissingInFamilyCount() > o.getMissingInFamilyCount()) {
			return 1;
		}
		
		if(this.getMatchCount() > o.getMatchCount()) {
			return -1;
		}
		if(this.getMatchCount() < o.getMatchCount()) {
			return 1;
		}
		
		if(this.getInFamilyButNotInImageCount() < o.getInFamilyButNotInImageCount()) {
			return -1;
		}
		if(this.getInFamilyButNotInImageCount() > o.getInFamilyButNotInImageCount()) {
			return 1;
		}
		return 0;
	}
	
}
