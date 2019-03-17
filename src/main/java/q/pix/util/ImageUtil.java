package q.pix.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
		return blankImage(IMAGE_SIZE, IMAGE_SIZE);
	}
	
	public static BufferedImage blankImage(int width, int height) {
		BufferedImage output = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);

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
	
	public static void combineImages(String rightImageDir, Optional<String> leftImageDir, boolean blanksWhereMissing, String outputDir) throws IOException {
		if(!leftImageDir.isPresent() && !blanksWhereMissing) {
			throw new IllegalArgumentException("Either the dir containing the second set of images must exist or I must be allowed to generate blanks where they are missing");
		}
		List<File> inputs = Arrays.asList(new File(rightImageDir).listFiles());
		new File(outputDir).mkdir();
		for(File input : inputs) {
			File target = null;
			if(leftImageDir.isPresent()) {
				target = new File(leftImageDir.get()+File.separator+input.getName());
				if(!target.exists() && !blanksWhereMissing) {
					continue;
				}
			}
			BufferedImage inputImage = ImageIO.read(input);
			BufferedImage targetImage = null;
			if(target != null && target.exists()) {
				targetImage = ImageIO.read(target);
			} else {
				targetImage = blankImage(inputImage.getWidth(), inputImage.getHeight());
			}
			combineImage(targetImage, inputImage, outputDir, input.getName());
		}
	}
	
	public static void combineImage(BufferedImage leftImage, BufferedImage rightImage, String outputDir, String outputNameBase) throws IOException {
		
		int cols = leftImage.getWidth() > IMAGE_SIZE ? leftImage.getWidth()/(IMAGE_SIZE/2) : 1;
		int rows = leftImage.getHeight() > IMAGE_SIZE ? leftImage.getHeight()/(IMAGE_SIZE/2) : 1;
		
		for(int c = 0;c < cols; c++) {
			for(int r = 0;r < rows; r++) {
				BufferedImage output = new BufferedImage(IMAGE_SIZE*2, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
				output.getGraphics().drawImage(leftImage, 0, 0, IMAGE_SIZE, IMAGE_SIZE, c*IMAGE_SIZE/2, r*IMAGE_SIZE/2, c*IMAGE_SIZE/2+IMAGE_SIZE, r*IMAGE_SIZE/2+IMAGE_SIZE, null);
				output.getGraphics().drawImage(rightImage, IMAGE_SIZE, 0, IMAGE_SIZE*2, IMAGE_SIZE, c*IMAGE_SIZE/2, r*IMAGE_SIZE/2, c*IMAGE_SIZE/2+IMAGE_SIZE, r*IMAGE_SIZE/2+IMAGE_SIZE, null);
				String namePrefix = (c == 1 && r == 1) ? "" : "_"+r*IMAGE_SIZE/2+"_"+c*IMAGE_SIZE/2+"_";
				ImageIO.write(output, "png", new File(outputDir+File.separator+namePrefix+outputNameBase));
			}
		}
		

	}
	
	public static void makeTrainSet(String dir) throws IOException {
		List<String> contents = Arrays.asList(new File(dir).list());
		if(!contents.contains(FileUtil.INPUT_DIR) || !contents.contains(FileUtil.TARGET_DIR)) {
			throw new IllegalArgumentException("Directory must contain input and target subdirectories");
		}
		String outDir = dir+File.separator+FileUtil.TRAIN_DIR; 
		combineImages(dir+File.separator+FileUtil.INPUT_DIR, Optional.ofNullable(dir+File.separator+FileUtil.TARGET_DIR), false, outDir);
	}
	
	public static void makeGenerationInputs(String dir) throws IOException {
		combineImages(dir, Optional.empty(),true, FileUtil.inputToTestsetOutputDir(dir));
	}
	
	private static BufferedImage loadOptional(Optional<String> path) throws IOException {
		if(!path.isPresent() || !new File(path.get()).exists()) {
			return blankImage();
		}
		return ImageIO.read(new File(path.get()));
	}
	
	public static Optional<Integer> deriveBackgroundColor(BufferedImage image) {
		int ul = image.getRGB(0, 0);
		if(ul == image.getRGB(image.getWidth()-1, image.getHeight()-1) &&
				ul == image.getRGB(0, image.getHeight()-1) &&
				ul == image.getRGB(image.getWidth()-1, 0)) {
			return Optional.of(ul);
		}
		return Optional.empty();
	}
	
	
	public static double colorDiff(int rgb1, int rgb2) {
		double rd = getRed(rgb2)-getRed(rgb1);
		rd = rd*rd;
		double gd = getGreen(rgb2)-getGreen(rgb1);
		gd = gd*gd;
		double bd = getBlue(rgb2)-getBlue(rgb1);
		bd = bd*bd;
		return Math.sqrt(rd+gd+bd);
	}
}