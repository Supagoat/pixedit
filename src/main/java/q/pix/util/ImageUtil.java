package q.pix.util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import org.imgscalr.Scalr;

import q.pix.AppState;

public class ImageUtil {

	public static BufferedImage loadAndScale(File imageFile) {
		try {
			return downscale(ImageIO.read(imageFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage downscale(BufferedImage input) {
		BufferedImage scaled = input;

		if (scaled.getWidth() > AppState.IMAGE_SIZE || scaled.getHeight() > AppState.IMAGE_SIZE) {
			scaled = Scalr.resize(input, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, AppState.IMAGE_SIZE,
					AppState.IMAGE_SIZE);
		}
		BufferedImage output = new BufferedImage(AppState.IMAGE_SIZE, AppState.IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);

		int background = Color.WHITE.getRGB();
		for(int x=0;x<output.getWidth();x++) {
			for(int y=0;y<output.getHeight();y++) {
				output.setRGB(x, y, background);
			}
		}
		
		for(int x=0;x<scaled.getWidth();x++) {
			for(int y=0;y<scaled.getHeight();y++) {
				output.setRGB(calcOffset(scaled.getWidth())+x, calcOffset(scaled.getHeight())+y, scaled.getRGB(x, y));
			}
		}
		
		return output;
	}

	public static int calcOffset(int size) {
		return (AppState.IMAGE_SIZE - size) / 2;
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