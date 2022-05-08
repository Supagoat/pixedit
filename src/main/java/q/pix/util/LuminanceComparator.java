package q.pix.util;

import java.util.Comparator;

import org.color4j.colorimetry.encodings.CIELab;

public class LuminanceComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		CIELab l1 = ImageUtil.hexToLab(o1);
		CIELab l2 = ImageUtil.hexToLab(o2);
		
		if(l1.getL() < l2.getL()) {
			return -1;
		}
		if(l1.getL() > l2.getL()) {
			return 1;
		}
		return 0;
	}

	
}
