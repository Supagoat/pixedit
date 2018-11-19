package q.pix.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class ImageUtil {
	public static final int IMAGE_SIZE = 256;
	
	public static BufferedImage loadAndScale(File imageFile) {
		try {
			return downscale(ImageIO.read(imageFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage downscale(BufferedImage input) {
		BufferedImage scaled = input;

		if (scaled.getWidth() > IMAGE_SIZE || scaled.getHeight() > IMAGE_SIZE) {
			scaled = Scalr.resize(input, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, IMAGE_SIZE,
					IMAGE_SIZE);
		}
		BufferedImage output = blankImage();
		
		for(int x=0;x<scaled.getWidth();x++) {
			for(int y=0;y<scaled.getHeight();y++) {
				output.setRGB(calcOffset(scaled.getWidth())+x, calcOffset(scaled.getHeight())+y, scaled.getRGB(x, y));
			}
		}
		
		return output;
	}
	
	public static BufferedImage blankImage() {
		BufferedImage output = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);

		int background = Color.WHITE.getRGB();
		for(int x=0;x<output.getWidth();x++) {
			for(int y=0;y<output.getHeight();y++) {
				output.setRGB(x, y, background);
			}
		}
		return output;
	}

	public static int calcOffset(int size) {
		return (IMAGE_SIZE - size) / 2;
	}

	public static int getRed(int rgb) {
		return (rgb >> 16) & 0x000000FF;
	}
	
	public static int getGreen(int rgb) {
		return  (rgb >>8 ) & 0x000000FF;
	}
	
	public static int getBlue(int rgb) {
		return rgb & 0x000000FF;
	}
	
}