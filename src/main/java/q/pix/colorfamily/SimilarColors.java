package q.pix.colorfamily;

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
		lab1[0]= 100; // we only want to compare color, not luminocity
		lab2[0] = 100;
		double similarity = ColorSimilarity.DeltaE00(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);
		return similarity;
	}
	
	public Color getLumOffsetColor() {
		double[] lab1 = ColorSimilarity.RGBA2LAB(getC1().getRed(),getC1().getGreen(), getC1().getBlue(), 1.0);
		double[] lab2 = ColorSimilarity.RGBA2LAB(getC2().getRed(),getC2().getGreen(), getC2().getBlue(), 1.0);
		
		double[] rgb1 = new double[] {getC1().getRed(),getC1().getGreen(), getC1().getBlue(), 1.0};
		double[] rgb2 = new double[] {getC2().getRed(),getC2().getGreen(), getC2().getBlue(), 1.0};
		
		if(lab1[0] > lab2[0]) {
			return darkTo(rgb1, rgb2, lab1, lab2);
		} else if (lab1[0] < lab2[0]) {
			return brightTo(rgb1, rgb2, lab1, lab2);
		}
		return getC1();
	}
	
	public Color brightTo(double[] rgb1, double[] rgb2, double[] lab1, double[] lab2) {
		boolean brightEnough = false;
		while(!brightEnough) {
			rgb1 = ColorSimilarity.GetBrighterColour(rgb1[0], rgb1[1], rgb1[2], 1, .01);
			double[] brighterLab = ColorSimilarity.RGBA2LAB(rgb1[0], rgb1[1], rgb1[2], 1);
			if(brighterLab[0] == lab1[0] || lab1[0] >= lab2[0]) {
				brightEnough = true;
			} else {
				lab1 = brighterLab;
			}
		}
		return new Color((int)Math.min(255,Math.round(rgb1[0])), (int)Math.min(255,Math.round(rgb1[1])),(int)Math.min(255,Math.round(rgb1[2])));
	}
	
	public Color darkTo(double[] rgb1, double[] rgb2, double[] lab1, double[] lab2) {

		
		boolean darkEnough = false;
		while(!darkEnough) {
			rgb1 = ColorSimilarity.GetDarkerColour(rgb1[0], rgb1[1], rgb1[2], 1, .01);
			double[] darkerLab = ColorSimilarity.RGBA2LAB(rgb1[0], rgb1[1], rgb1[2], 1);
			if(darkerLab[0] == lab1[0] || lab1[0] <= lab2[0]) {
				darkEnough = true;
			} else {
				lab1 = darkerLab;
			}
		}
		return new Color((int)Math.max(0,Math.round(rgb1[0])), (int)(int)Math.max(0,Math.round(rgb1[1])),(int)(int)Math.max(0,Math.round(rgb1[2])));
	}
	
	/*
	public Color getLumOffsetColor() {
		double[] lab1 = ColorSimilarity.RGBA2LAB(getC1().getRed(),getC1().getGreen(), getC1().getBlue(), 1.0);
		double[] lab2 = ColorSimilarity.RGBA2LAB(getC2().getRed(),getC2().getGreen(), getC2().getBlue(), 1.0);
		//lab1[0] = lab2[0];
		
		double[] xyz1 = ColorSimilarity.RGB2XYZ(getC1().getRed(),getC1().getGreen(), getC1().getBlue(), 1);
		double[] xyz2 = ColorSimilarity.RGB2XYZ(getC2().getRed(),getC2().getGreen(), getC2().getBlue(), 1.0);
		xyz1[1] = xyz2[1];
		
		double[] labVer = ColorSimilarity.XYZ2LAB(xyz1[0], xyz1[1], xyz1[2]);
		
		//double[] newColorValues = ColorSimilarity.LAB2RGBA(lab1[0], lab1[1], lab1[2]);
		//double[] newColorValues = ColorSimilarity.XYZ2RGBA(xyz1[0], xyz1[1], xyz1[2]);
		double[] newColorValues = ColorSimilarity.LAB2RGBA(labVer[0], labVer[1], labVer[2]);
		newColorValues[0] = newColorValues[0] < 0 ? 0 : newColorValues[0];
		newColorValues[1] = newColorValues[1] < 0 ? 0 : newColorValues[1];
		newColorValues[2] = newColorValues[2] < 0 ? 0 : newColorValues[2];
		return new Color(Math.round(newColorValues[0]), Math.round(newColorValues[1]), Math.round(newColorValues[2]));
	}
	*/
	// For use with a comparator
	public int getBrighterColor() {
		double[] lab1 = ColorSimilarity.RGBA2LAB(getC1().getRed(),getC1().getGreen(), getC1().getBlue(), 1.0);
		double[] lab2 = ColorSimilarity.RGBA2LAB(getC2().getRed(),getC2().getGreen(), getC2().getBlue(), 1.0);
		return Double.valueOf(lab1[0]).compareTo(Double.valueOf(lab2[0]));
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
