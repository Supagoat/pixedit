package external;

/**
 * This is a conversion of version 1.5 of "IsThisColourSimilar" Javascript
 * implementation by Ahmed Moussa <moussa.ahmed95@gmail.com> The original can be
 * found at
 * https://github.com/hamada147/IsThisColourSimilar/blob/master/Colour.js
 *
 * It is offered with the Apache 2.0 license Represent the color object and
 * it's different types (HEX, RGBA, XYZ, LAB) This class have the ability to do
 * the following 1. Convert HEX to RGBA 2. Convert RGB to XYZ 3. Convert XYZ to
 * LAB 4. Calculate Delta E00 between two LAB color (Main purpose)
 */

public class ColorSimilarity {
	/**
	 * Convert HEX to LAB
	 * 
	 * @param {[string]} hex hex colour value desired to be converted to LAB
	 */
//		static double[] HEX2LAB(String hex) {
//			double[] RGBa =  HEX2RGBA(hex);
//			double[] xyz =   RGB2XYZ(R, G, b, a);
//			double lab[] =  XYZ2LAB(X, Y, Z);
//			return lab;
//		}
	/**
	 * Convert RGBA to LAB
	 * 
	 * @param {[Number]} R Red value from 0 to 255
	 * @param {[Number]} G Green value from 0 to 255
	 * @param {[Number]} B Blue value from 0 to 255
	 * @param {[Number]} a Optional alpha 0 to 1
	 */
	public static double[] RGBA2LAB(double R, double G, double B, double a) {
		double[] xyz = RGB2XYZ(R, G, B, a);
		double lab[] = XYZ2LAB(xyz[0], xyz[1], xyz[2]);
		return lab;
	}

	/**
	 * Convert LAB to RGBA
	 * 
	 * @param {[Number]} L
	 * @param {[Number]} A
	 * @param {[Number]} B
	 */
	public static double[] LAB2RGBA(double L, double A, double B) {
		double[] xyz = LAB2XYZ(L, A, B);
		double[] RGba = XYZ2RGBA(xyz[0], xyz[1], xyz[2]);
		return RGba;
	}

	/**
	 * Convert HEX to RGBA
	 * 
	 * @param {[string]} hex hex colour value desired to be converted to RGBA NOTE:
	 *                   Untested, I'm not using hex and I haven't bothered to spend
	 *                   time to test this one
	 */
//		static double[] HEX2RGBA(String hex) {
//			double[] c = new double[4];
//			if (hex.charAt(0) == "#") {
//				hex = hex.substring(1, hex.length());
//			}
//			if (hex.length > 6 || hex.length < 3) {
//				throw new IllegalArgumentException("HEX colour must be 3 or 6 values. You provided "+c.length());
//			}
//			if (c.length == 3) {
//				c = [hex[0], hex[0], hex[1], hex[1], hex[2], hex[2]];
//			}
//			c = "0x" + c.join("");
//			let R = (c >> 16) & 255;
//			let G = (c >> 8) & 255;
//			let B = c & 255;
//			let A = 1;
//			return [R, G, B, A];
//		}
	/**
	 * Convert RGB to XYZ
	 * 
	 * @param {[Number]} R Red value from 0 to 255
	 * @param {[Number]} G Green value from 0 to 255
	 * @param {[Number]} B Blue value from 0 to 255
	 * @param {Number}   [A=1] Obacity value from 0 to 1 with a default value of 1
	 *                   if not sent
	 */
	public static double[] RGB2XYZ(double R, double G, double B, double A) {
		if (R > 255) {
			//System.out.println("Red value was higher than 255. It has been set to 255.");
			R = 255.0;
		} else if (R < 0) {
			//System.out.println("Red value was smaller than 0. It has been set to 0.");
			R = 0.0;
		}
		if (G > 255) {
			//System.out.println("Green value was higher than 255. It has been set to 255.");
			G = 255.0;
		} else if (G < 0) {
			//System.out.println("Green value was smaller than 0. It has been set to 0.");
			G = 0.0;
		}
		if (B > 255) {
			//System.out.println("Blue value was higher than 255. It has been set to 255.");
			B = 255.0;
		} else if (B < 0) {
			//System.out.println("Blue value was smaller than 0. It has been set to 0.");
			B = 0.0;
		}
		if (A > 1) {
			//System.out.println("Obacity value was higher than 1. It has been set to 1.");
			A = 1.0;
		} else if (A < 0) {
			//System.out.println("Obacity value was smaller than 0. It has been set to 0.");
			A = 0.0;
		}
		double r = R / 255.0;
		double g = G / 255.0;
		double b = B / 255.0;
		// step 1
		if (r > 0.04045) {
			r = Math.pow(((r + 0.055) / 1.055), 2.4);
		} else {
			r = r / 12.92;
		}
		if (g > 0.04045) {
			g = Math.pow(((g + 0.055) / 1.055), 2.4);
		} else {
			g = g / 12.92;
		}
		if (b > 0.04045) {
			b = Math.pow(((b + 0.055) / 1.055), 2.4);
		} else {
			b = b / 12.92;
		}
		// step 2
		r = r * 100;
		g = g * 100;
		b = b * 100;
		// step 3
		double X = (r * 0.4124) + (g * 0.3576) + (b * 0.1805);
		double Y = (r * 0.2126) + (g * 0.7152) + (b * 0.0722);
		double Z = (r * 0.0193) + (g * 0.1192) + (b * 0.9505);
		return new double[] { X, Y, Z };
	}

	/**
	 * Convert XYZ to RGBA
	 * 
	 * @param {[double]} X
	 * @param {[double]} Y
	 * @param {[double]} Z
	 */
	public static double[] XYZ2RGBA(double X, double Y, double Z) {
		double var_X = X / 100;
		double var_Y = Y / 100;
		double var_Z = Z / 100;

		double var_R = (var_X * 3.2406) + (var_Y * -1.5372) + (var_Z * -0.4986);
		double var_G = (var_X * -0.9689) + (var_Y * 1.8758) + (var_Z * 0.0415);
		double var_B = (var_X * 0.0557) + (var_Y * -0.2040) + (var_Z * 1.0570);

		if (var_R > 0.0031308) {
			var_R = 1.055 * Math.pow(var_R, (1.0 / 2.4)) - 0.055;
		} else {
			var_R = 12.92 * var_R;
		}
		if (var_G > 0.0031308) {
			var_G = 1.055 * Math.pow(var_G, (1.0 / 2.4)) - 0.055;
		} else {
			var_G = 12.92 * var_G;
		}
		if (var_B > 0.0031308) {
			var_B = 1.055 * Math.pow(var_B, (1.0 / 2.4)) - 0.055;
		} else {
			var_B = 12.92 * var_B;
		}

		double R = Math.round(var_R * 255);
		double G = Math.round(var_G * 255);
		double B = Math.round(var_B * 255);

		return new double[] { R, G, B, 1 };
	}

	/**
	 * Convert XYZ to LAB
	 * 
	 * @param {[Number]} X Value
	 * @param {[Number]} Y Value
	 * @param {[Number]} Z Value
	 */
	public static double[] XYZ2LAB(double X, double Y, double Z) {
		// using 10o Observer (CIE 1964)
		// CIE10_D65 = {94.811f, 100f, 107.304f} => Daylight
		double ReferenceX = 94.811;
		double ReferenceY = 100.0;
		double ReferenceZ = 107.304;
		// step 1
		double x = X / ReferenceX;
		double y = Y / ReferenceY;
		double z = Z / ReferenceZ;
		// step 2
		if (x > 0.008856) {
			x = Math.pow(x, (1.0 / 3.0));
		} else {
			x = (7.787 * x) + (16.0 / 116.0);
		}
		if (y > 0.008856) {
			y = Math.pow(y, (1.0 / 3.0));
		} else {
			y = (7.787 * y) + (16.0 / 116.0);
		}
		if (z > 0.008856) {
			z = Math.pow(z, (1.0 / 3.0));
		} else {
			z = (7.787 * z) + (16.0 / 116.0);
		}
		// step 3
		double L = (116.0 * y) - 16.0;
		double A = 500.0 * (x - y);
		double B = 200.0 * (y - z);
		return new double[] { L, A, B };
	}

	/**
	 * Convert LAB to XYZ
	 * 
	 * @param {[Number]} L
	 * @param {[Number]} A
	 * @param {[Number]} B
	 */
	public static double[] LAB2XYZ(double L, double A, double B) {
		// using 10o Observer (CIE 1964)
		// CIE10_D65 = {94.811f, 100f, 107.304f} => Daylight
		double ReferenceX = 94.811;
		double ReferenceY = 100.0;
		double ReferenceZ = 107.304;

		double var_Y = (L + 16.0) / 116.0;
		double var_X = A / 500.0 + var_Y;
		double var_Z = var_Y - B / 200.0;

		if (Math.pow(var_Y, 3.0) > 0.008856) {
			var_Y = Math.pow(var_Y, 3.0);
		} else {
			var_Y = (var_Y - 16.0 / 116.0) / 7.787;
		}
		if (Math.pow(var_X, 3.0) > 0.008856) {
			var_X = Math.pow(var_X, 3.0);
		} else {
			var_X = (var_X - 16.0 / 116.0) / 7.787;
		}
		if (Math.pow(var_Z, 3.0) > 0.008856) {
			var_Z = Math.pow(var_Z, 3.0);
		} else {
			var_Z = (var_Z - 16.0 / 116.0) / 7.787;
		}

		double X = var_X * ReferenceX;
		double Y = var_Y * ReferenceY;
		double Z = var_Z * ReferenceZ;

		return new double[] { X, Y, Z };
	}

	/**
	 * The difference between two given colours with respect to the human eye
	 * 
	 * @param {[type]} l1 Colour 1
	 * @param {[type]} a1 Colour 1
	 * @param {[type]} b1 Colour 1
	 * @param {[type]} l2 Colour 2
	 * @param {[type]} a2 Colour 2
	 * @param {[type]} b2 Colour 2
	 */
	public static double DeltaE00(double l1, double a1, double b1, double l2, double a2, double b2) {
		// Start Equation
		// Equation exist on the following URL
		// http://www.brucelindbloom.com/index.html?Eqn_DeltaE_CIE2000.html
		double avgL = (l1 + l2) / 2.0;
		double C1 = Math.sqrt(Math.pow(a1, 2.0) + Math.pow(b1, 2.0));
		double C2 = Math.sqrt(Math.pow(a2, 2.0) + Math.pow(b2, 2.0));
		double avgC = (C1 + C2) / 2.0;
		double G = (1 - Math.sqrt(Math.pow(avgC, 7.0) / (Math.pow(avgC, 7.0) + Math.pow(25.0, 7.0)))) / 2.0;

		double A1p = a1 * (1.0 + G);
		double A2p = a2 * (1.0 + G);

		double C1p = Math.sqrt(Math.pow(A1p, 2.0) + Math.pow(b1, 2.0));
		double C2p = Math.sqrt(Math.pow(A2p, 2.0) + Math.pow(b2, 2.0));

		double avgCp = (C1p + C2p) / 2.0;

		double h1p = Math.toDegrees(Math.atan2(b1, A1p));
		if (h1p < 0.0) {
			h1p = h1p + 360.0;
		}

		double h2p = Math.toDegrees(Math.atan2(b2, A2p));
		if (h2p < 0.0) {
			h2p = h2p + 360.0;
		}

		double avghp = Math.abs(h1p - h2p) > 180.0 ? (h1p + h2p + 360.0) / 2.0 : (h1p + h1p) / 2.0;

		double T = 1.0 - 0.17 * Math.cos(Math.toRadians(avghp - 30.0)) + 0.24 * Math.cos(Math.toRadians(2.0 * avghp))
				+ 0.32 * Math.cos(Math.toRadians(3.0 * avghp + 6.0)) - 0.2 * Math.cos(Math.toRadians(4.0 * avghp - 63.0));

		double deltahp = h2p - h1p;
		if (Math.abs(deltahp) > 180.0) {
			if (h2p <= h1p) {
				deltahp += 360.0;
			} else {
				deltahp -= 360.0;
			}
		}

		double delta_lp = l2 - l1;
		double delta_cp = C2p - C1p;

		deltahp = 2.0 * Math.sqrt(C1p * C2p) * Math.sin(Math.toRadians(deltahp) / 2.0);

		double Sl = 1.0 + ((0.015 * Math.pow(avgL - 50.0, 2.0)) / Math.sqrt(20.0 + Math.pow(avgL - 50.0, 2.0)));
		double Sc = 1.0 + 0.045 * avgCp;
		double Sh = 1.0 + 0.015 * avgCp * T;

		double deltaro = 30.0 * Math.exp(-(Math.pow((avghp - 275.0) / 25.0, 2.0)));
		double Rc = 2.0 * Math.sqrt(Math.pow(avgCp, 7.0) / (Math.pow(avgCp, 7.0) + Math.pow(25.0, 7.0)));
		double Rt = -Rc * Math.sin(2.0 * Math.toRadians(deltaro));

		double kl = 1.0;
		double kc = 1.0;
		double kh = 1.0;

		double deltaE = Math.sqrt(Math.pow(delta_lp / (kl * Sl), 2.0) + Math.pow(delta_cp / (kc * Sc), 2.0)
				+ Math.pow(deltahp / (kh * Sh), 2.0) + Rt * (delta_cp / (kc * Sc)) * (deltahp / (kh * Sh)));

		return deltaE;
	}

	/**
	 * Get darker colour of the given colour
	 * 
	 * @param {[Number]} R Red value from 0 to 255
	 * @param {[Number]} G Green value from 0 to 255
	 * @param {[Number]} B Blue value from 0 to 255
	 */
	public static double[] GetDarkerColour(double r, double g, double b, double a, double darkenPercentage) {// = 0.05) {
		double[] lab = RGBA2LAB(r, g, b, a);
		lab[0] -= lab[0] * darkenPercentage;
		if (lab[0] < 0) {
			lab[0] = 0.0;
		}
		double[] RGBA = LAB2RGBA(lab[0], lab[1], lab[2]);
		return RGBA;
	}

	/**
	 * Get brighter colour of the given colour
	 * 
	 * @param {[Number]} R Red value from 0 to 255
	 * @param {[Number]} G Green value from 0 to 255
	 * @param {[Number]} B Blue value from 0 to 255
	 */
	public static double[] GetBrighterColour(double r, double g, double b, double a, double brighterPercentage) { // = 0.05) {
		double[] lab = RGBA2LAB(r, g, b, a);
		lab[0] += lab[0] * brighterPercentage;
		if (lab[0] > 100.0) {
			lab[0] = 100.0;
		}
		double[] RGBA = LAB2RGBA(lab[0], lab[1], lab[2]);
		return RGBA;
	}
}
