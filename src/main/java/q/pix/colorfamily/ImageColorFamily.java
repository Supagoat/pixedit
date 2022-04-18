package q.pix.colorfamily;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageColorFamily {
	//private ColorFamily baseFamily;
	private Map<Integer, FamilyCount> familyIdxToPixCount = new HashMap<>();
	private Map<Integer, Integer> groupNumberToFrequencyRank  = new HashMap<>();
	//private List<FamilyCount> familyCounts = new ArrayList<>();
	public ImageColorFamily(ColorFamily baseFamily, BufferedImage img) {
		for(int y=0;y<img.getHeight();y++) {
			for(int x=0;x<img.getWidth();x++) {
				Color c = new Color(img.getRGB(x, y));
				int group = baseFamily.getColorGroup(c);
				if(!familyIdxToPixCount.containsKey((group))) {
					familyIdxToPixCount.put(group, new FamilyCount(group, 0));
				}
				familyIdxToPixCount.get(group).incr();
			}
		}
		
		List<FamilyCount> familyCounts = new ArrayList<>();
		for(FamilyCount f : familyIdxToPixCount.values()) {
			familyCounts.add(f);
		}
		Collections.sort(familyCounts);
		for(int i=0;i<familyCounts.size();i++) {
			groupNumberToFrequencyRank.put(familyCounts.get(i).getGroupNum(), i);
		}
		
	}
	
	public int getGroupNumRank(int groupNum) {
		return groupNumberToFrequencyRank.get(groupNum);
	}
	
	
	private class FamilyCount implements Comparable {
		private int groupNum;
		private int count;
		
		public FamilyCount(int groupNum, int count) {
			this.setGroupNum(groupNum);
			this.setCount(count);
		}
		
		public void incr() {
			count++;
		}

		public int getGroupNum() {
			return groupNum;
		}

		public void setGroupNum(int groupNum) {
			this.groupNum = groupNum;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public int compareTo(Object o) {
			FamilyCount fc = (FamilyCount)o;
			if(this.getCount() < fc.getCount()) {
				return -1;
			}
			if(this.getCount() > fc.getCount()) {
				return 1;
			}
			return 0;
		}
	}
}