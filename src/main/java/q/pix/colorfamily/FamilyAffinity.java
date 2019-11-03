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
	private List<Set<Color>> colorFamily;
	
	public FamilyAffinity(int matchCount, int missingInFamilyCount, int inFamilyButNotInImageCount, List<Set<Color>> colorFamily) {
		this.matchCount = matchCount;
		this.missingInFamilyCount = missingInFamilyCount;
		this.inFamilyButNotInImageCount = inFamilyButNotInImageCount;
		this.colorFamily = colorFamily;
	}

	public boolean isMatchingAffinity(Set<Color> colors) {
		Long inFamily = colors.stream().filter(c -> ImageUtil.hasFamily(c, getColorFamily())).collect(Collectors.counting());
		return inFamily == null ? false : inFamily.doubleValue()/colors.size() > 0.5;
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

	public List<Set<Color>> getColorFamily() {
		return colorFamily;
	}

	public void setColorFamily(List<Set<Color>> colorFamily) {
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
		
		if(this.getMatchCount() < o.getMatchCount()) {
			return -1;
		}
		if(this.getMatchCount() > o.getMatchCount()) {
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
