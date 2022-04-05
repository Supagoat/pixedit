package q.pix.colorfamily;

import java.awt.Color;

public class SimilarTester {

	public static void main(String[] args) {
		Color c1 = new Color(0,0,1);
		Color c2 = new Color (96,0,0);
		SimilarColors s = new SimilarColors(c1,c2);
		System.out.println(s.getDiff());
	}
}
