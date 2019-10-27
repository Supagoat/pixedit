package q.pix.util;

import java.awt.Color;

import external.ColorSimilarity;

public class SimilarColors {
	public Color c1;
	public Color c2;
	
	public SimilarColors(Color c1, Color c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
	
	public double getDiff() {
		double[] lab1 = ColorSimilarity.RGBA2LAB(getC1().getRed(),getC1().getGreen(), getC1().getBlue(), 1.0);
		double[] lab2 = ColorSimilarity.RGBA2LAB(getC2().getRed(),getC2().getGreen(), getC2().getBlue(), 1.0);
		lab1[0]= 100;
		lab2[0] = 100;
		double similarity = ColorSimilarity.DeltaE00(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);
		return similarity;
	}
	
	public boolean containsColor(Color c) {
		return c.equals(getC1()) || c.equals(getC2());
	}
	
	public Color getC1() {
		return c1;
	}
	public void setC1(Color c1) {
		this.c1 = c1;
	}
	public Color getC2() {
		return c2;
	}
	public void setC2(Color c2) {
		this.c2 = c2;
	}
	
	@Override
	public int hashCode() {
		return getC1().hashCode()+getC2().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof SimilarColors) {
			SimilarColors oc = (SimilarColors)o;
			return (oc.getC1().equals(oc.getC1()) && oc.getC2().equals(oc.getC2())) ||
					(oc.getC1().equals(oc.getC2()) && oc.getC2().equals(oc.getC1())) ||
					(oc.getC2().equals(oc.getC1()) && oc.getC1().equals(oc.getC2()));
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getC1()+" "+getC2()+" "+getDiff();
	}
}
