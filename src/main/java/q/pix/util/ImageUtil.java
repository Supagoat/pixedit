package q.pix.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class ImageUtil {
	public static final int IMAGE_WIDTH = 256;
	public static final int IMAGE_HEIGHT = 256;

	public static BufferedImage loadAndScale(File imageFile) {
		try {
			return downscale(ImageIO.read(imageFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage downscale(BufferedImage input) {
		BufferedImage scaled = input;

		if (scaled.getWidth() > IMAGE_WIDTH || scaled.getHeight() > IMAGE_HEIGHT) {
			scaled = Scalr.resize(input, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, IMAGE_WIDTH, IMAGE_HEIGHT);
		}
		BufferedImage output = blankImage();

		for (int x = 0; x < scaled.getWidth(); x++) {
			for (int y = 0; y < scaled.getHeight(); y++) {
				output.setRGB(calcOffsetWidth(scaled.getWidth()) + x, calcOffsetHeight(scaled.getHeight()) + y,
						scaled.getRGB(x, y));
			}
		}

		return output;
	}

	public static BufferedImage blankImage(Color color) {
		return blankImage(IMAGE_WIDTH, IMAGE_HEIGHT, color);
	}

	public static BufferedImage blankImage() {
		return blankImage(IMAGE_WIDTH, IMAGE_HEIGHT, Color.WHITE);
	}

	public static BufferedImage blankImage(int width, int height, Color backgroundColor) {
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				output.setRGB(x, y, backgroundColor.getRGB());
			}
		}
		return output;
	}

	public static int calcOffsetWidth(int size) {
		return (IMAGE_WIDTH - size) / 2;
	}

	public static int calcOffsetHeight(int size) {
		return (IMAGE_HEIGHT - size) / 2;
	}

	public static int getRed(int rgb) {
		return (rgb >> 16) & 0x000000FF;
	}

	public static int getGreen(int rgb) {
		return (rgb >> 8) & 0x000000FF;
	}

	public static int getBlue(int rgb) {
		return rgb & 0x000000FF;
	}

	public static void combineImages(String rightImageDir, Optional<String> leftImageDir, boolean blanksWhereMissing,
			String outputDir) throws IOException {
		if (!leftImageDir.isPresent() && !blanksWhereMissing) {
			throw new IllegalArgumentException(
					"Either the dir containing the second set of images must exist or I must be allowed to generate blanks where they are missing");
		}
		List<File> inputs = Arrays.asList(new File(rightImageDir).listFiles());
		new File(outputDir).mkdir();
		for (File input : inputs) {
			File target = null;
			if (leftImageDir.isPresent()) {
				target = new File(leftImageDir.get() + File.separator + input.getName());
				if (!target.exists() && !blanksWhereMissing) {
					continue;
				}
			}
			BufferedImage inputImage = ImageIO.read(input);
			BufferedImage targetImage = null;
			if (target != null && target.exists()) {
				targetImage = ImageIO.read(target);
			} else {
				targetImage = blankImage(inputImage.getWidth(), inputImage.getHeight(), Color.WHITE);
			}
			combineImage(targetImage, inputImage, outputDir, input.getName());
		}
	}

	public static void combineImage(BufferedImage leftImage, BufferedImage rightImage, String outputDir,
			String outputNameBase) throws IOException {

		int cols = leftImage.getWidth() > IMAGE_WIDTH ? leftImage.getWidth() / (IMAGE_WIDTH / 2) : 1;
		int rows = leftImage.getHeight() > IMAGE_HEIGHT ? leftImage.getHeight() / (IMAGE_HEIGHT / 2) : 1;

		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++) {
				BufferedImage output = new BufferedImage(IMAGE_WIDTH * 2, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
				output.getGraphics().drawImage(leftImage, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, c * IMAGE_WIDTH / 2,
						r * IMAGE_HEIGHT / 2, c * IMAGE_WIDTH / 2 + IMAGE_WIDTH, r * IMAGE_HEIGHT / 2 + IMAGE_HEIGHT,
						null);
				output.getGraphics().drawImage(rightImage, IMAGE_WIDTH, 0, IMAGE_WIDTH * 2, IMAGE_HEIGHT,
						c * IMAGE_WIDTH / 2, r * IMAGE_HEIGHT / 2, c * IMAGE_WIDTH / 2 + IMAGE_WIDTH,
						r * IMAGE_HEIGHT / 2 + IMAGE_HEIGHT, null);
				String namePrefix = (cols == 1 && rows == 1) ? ""
						: "_" + c * IMAGE_WIDTH / 2 + "_" + r * IMAGE_HEIGHT / 2 + "_";
				ImageIO.write(output, "png", new File(outputDir + File.separator + namePrefix + outputNameBase));
			}
		}

	}

	public static void makeTrainSet(String dir) throws IOException {
		List<String> contents = Arrays.asList(new File(dir).list());
		if (!contents.contains(FileUtil.INPUT_DIR) || !contents.contains(FileUtil.TARGET_DIR)) {
			throw new IllegalArgumentException("Directory must contain input and target subdirectories");
		}
		String outDir = dir + File.separator + FileUtil.TRAIN_DIR;
		combineImages(dir + File.separator + FileUtil.INPUT_DIR,
				Optional.ofNullable(dir + File.separator + FileUtil.TARGET_DIR), false, outDir);
	}

	public static void makeGenerationInputs(String dir) throws IOException {
		combineImages(dir, Optional.empty(), true, FileUtil.inputToTestsetOutputDir(dir));
	}

	private static BufferedImage loadOptional(Optional<String> path) throws IOException {
		if (!path.isPresent() || !new File(path.get()).exists()) {
			return blankImage();
		}
		return ImageIO.read(new File(path.get()));
	}

	public static Optional<Integer> deriveBackgroundColor(BufferedImage image) {
		int ul = image.getRGB(0, 0);
		if (ul == image.getRGB(image.getWidth() - 1, image.getHeight() - 1)
				&& ul == image.getRGB(0, image.getHeight() - 1) && ul == image.getRGB(image.getWidth() - 1, 0)) {
			return Optional.of(ul);
		}
		return Optional.empty();
	}

	public static void splitImages(File inputDir) throws IOException {
		File[] files = inputDir.listFiles();
		File outDir = new File(inputDir.getAbsolutePath() + File.separator + "splitOut");
		File smallOutputDir = new File(inputDir.getAbsolutePath() + File.separator + "splitOut_small");
		File largeOutputDir = new File(inputDir.getAbsolutePath() + File.separator + "splitOut_big");
		outDir.mkdir();
		smallOutputDir.mkdir();
		largeOutputDir.mkdir();

		for (File file : files) {
			if (file.getName().endsWith("png")) {
				splitFile(file, outDir.getAbsolutePath());
			}
		}
	}

	public static void splitFile(File inputFile, String outputDir) throws IOException {
		try {

			BufferedImage in = ImageIO.read(inputFile);
			int imageCt = 0;
			Set<Point> pointsSpokenFor = new HashSet<>();

			for (int y = 0; y < in.getHeight(); y++) {
				for (int x = 0; x < in.getWidth(); x++) {
					Point p = new Point(x, y);
					if (!isTransparent(in.getRGB(x, y)) && !pointsSpokenFor.contains(p)) {
						Set<Point> memberPoints = findTouchingNonTransparents(in, x, y);
						pointsSpokenFor.addAll(memberPoints);
						Point upperLeftBound = findUpperBound(memberPoints);
						Point lowerRightBound = findLowerBound(memberPoints);
						int width = lowerRightBound.x - upperLeftBound.x;
						int height = lowerRightBound.y - upperLeftBound.y;
						if (width > 4 && height > 4) {
							BufferedImage subImage = in.getSubimage(upperLeftBound.x, upperLeftBound.y,
									lowerRightBound.x - upperLeftBound.x, lowerRightBound.y - upperLeftBound.y);

							String outFileName = inputFile.getName().substring(0, inputFile.getName().indexOf('.')).replaceAll(" ","_")
									+ "split_" + imageCt + ".png";
							
							if (subImage.getWidth() <= 128 && subImage.getHeight() <= 128) {
								ImageIO.write(centerImageOnGreen(subImage, 128), "png",
										new File(outputDir + "_small" + File.separator + outFileName));
							} else if (subImage.getWidth() <= 256 && subImage.getHeight() <= 256) {
								ImageIO.write(centerImageOnGreen(subImage, 256), "png", new File(outputDir + File.separator + outFileName));
							} 
							imageCt++;
						}

					}
				}
			}
		} catch (Exception e) {
			System.err.println(inputFile.getName());
			e.printStackTrace();
		}

//		BufferedImage out = ImageUtil.blankImage(in.getWidth(), in.getHeight(),
//				new Color(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 0));
//		Graphics2D g = (Graphics2D) out.createGraphics();
//		AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
//		g.setComposite(composite);
//		for (int y = 1; y < out.getHeight() - 1; y++) {
//			for (int x = 1; x < out.getWidth() - 1; x++) {
//				if ((in.getRGB(x, y) & 0xff000000) != 0) {
//					boolean transparencyFound = false;
//					for (int yb = y - 1; yb < y + 2; yb++) {
//						for (int xb = x - 1; xb < x + 2; xb++) {
//							transparencyFound = transparencyFound || (in.getRGB(xb, yb) & 0xff000000) == 0;
//						}
//					}
//					if (transparencyFound) {
//						out.setRGB(x, y, background.getRGB());
//					} else {
//						out.setRGB(x, y, Color.WHITE.getRGB());
//					}
//				} else {
//					g.setColor(new Color(0, 0, 0, 0));
//					g.fillRect(x, y, 1, 1);
//				}
//			}
//		}
//		String outFileName = inputFile.getAbsolutePath().replace(".png", "_outlined.png");
		// ImageIO.write(out, "png", new File(outFileName));
	}

	public static BufferedImage centerImageOnGreen(BufferedImage in, int size) {
		Color background = new Color(0, 255, 0);
		BufferedImage out = ImageUtil.blankImage(size, size, background);
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				out.setRGB(x, y, background.getRGB());
			}
		}
		int offsetX = (size-in.getWidth())/2;
		int offsetY = (size-in.getHeight())/2;
		
		for (int y = 0; y < in.getHeight(); y++) {
			for (int x = 0; x < in.getWidth(); x++) {
				if(!isTransparent(in.getRGB(x, y))) {
					try {
					out.setRGB(x+offsetX, y+offsetY, in.getRGB(x, y));
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return out;
	}

	public static Point findUpperBound(Set<Point> points) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		for (Point p : points) {
			if (p.x < minX) {
				minX = p.x;
			}
			if (p.y < minY) {
				minY = p.y;
			}
		}
		return new Point(minX, minY);
	}

	public static Point findLowerBound(Set<Point> points) {
		int maxX = -1;
		int maxY = -1;
		for (Point p : points) {
			if (p.x > maxX) {
				maxX = p.x;
			}
			if (p.y > maxY) {
				maxY = p.y;
			}
		}
		return new Point(maxX, maxY);
	}

	public static Set<Point> findTouchingNonTransparents(BufferedImage i, int baseX, int baseY) {
		Set<Point> nonTransparents = new HashSet<>(1000);
		Set<Point> checked = new HashSet<>(1000);
//		int minX = i.getWidth();
//		int minY = i.getHeight();
//		int maxX = 0;
//		int maxY = 0;
		Set<Point> toCheck = new HashSet<>(100);

		addPoints(toCheck, new Point(baseX, baseY), checked, i.getWidth(), i.getHeight());

		while (!toCheck.isEmpty()) {
			Point p = toCheck.iterator().next();
			toCheck.remove(p);
			checked.add(p);
			if (!isTransparent(i.getRGB(p.x, p.y))) {
				nonTransparents.add(p);
				addPoints(toCheck, p, checked, i.getWidth(), i.getHeight());
			}

		}

		return nonTransparents;

	}

	private static void addPoints(Set<Point> addTo, Point basePoint, Set<Point> checked, int maxX, int maxY) {
		for (int y = basePoint.y - 1; y < basePoint.y + 2; y++) {
			for (int x = basePoint.x - 1; x < basePoint.x + 2; x++) {
				if (x < 0 || y < 0 || x >= maxX || y >= maxY) {
					continue;
				}
				Point p = new Point(x, y);
				if (!checked.contains(p)) {
					addTo.add(p);
				}
			}

		}

	}

	public static boolean isTransparent(int argb) {
		return (argb & 0xff000000) == 0;
	}

	public static void outlineFile(File inputFile) throws IOException {
		try {
		if(inputFile.isDirectory()) {
			File outDir = new File(inputFile.getAbsoluteFile()+"_outlined");
			outDir.mkdir();
			File[] files = inputFile.listFiles();
			for(int i=0;i<files.length;i++) {
				File f = files[i];
				if(f.getName().endsWith(".png")) {
					outlineFile(f, outDir.getAbsolutePath());
				}
			}
		} else {
			outlineSingleFile(inputFile);
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void outlineSingleFile(File inputFile) throws IOException {
		String outFileName = inputFile.getAbsolutePath().replace(".png", "_outlined.png");
		outlineFile(inputFile, outFileName);
	}
	
	public static void outlineFile(File inputFile, String outFilePath) throws IOException {

		BufferedImage in = ImageIO.read(inputFile);
		BufferedImage out = ImageUtil.blankImage(in.getWidth(), in.getHeight(),
				Color.WHITE);
		//Graphics2D g = (Graphics2D) out.createGraphics();
		//AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
		//g.setComposite(composite);
		for (int y = 1; y < out.getHeight() - 1; y++) {
			for (int x = 1; x < out.getWidth() - 1; x++) {
				if ((in.getRGB(x, y) & 0xff000000) != 0 && !isBackgroundColor(in.getRGB(x, y))) {
					boolean transparencyFound = false;
					for (int yb = y - 1; yb < y + 2; yb++) {
						for (int xb = x - 1; xb < x + 2; xb++) {
							transparencyFound = isBackgroundColor(in.getRGB(xb, yb)) || transparencyFound || (in.getRGB(xb, yb) & 0xff000000) == 0;
						}
					}
					if (transparencyFound) {
						out.setRGB(x, y, Color.BLACK.getRGB() | 0xff000000);
					} else {
						//no need for this out.setRGB(x, y, Color.WHITE.getRGB());
					}
				} else {
				//	g.setColor(new Color(0, 0, 0, 0));
				//	g.fillRect(x, y, 1, 1);
					//out.setRGB(x, y, Color.RED.getRGB() | 0xff000000);
				}
			}
		}
		
		//ImageIO.write(out, "png", new File(outFilePath));
		applyDoubleOutline(out, inputFile.getName(), outFilePath);
	}
	
	public static boolean isBackgroundColor(int rgb) {
		Color c = new Color(rgb);
		return (c.getRed() == 0 && c.getBlue() == 0 && c.getGreen() == 255);
	}

	public static void applyDoubleOutline(BufferedImage in, String inName, String outPath) throws IOException {
		findBackgroundColor(in);
		Color background = Color.WHITE;//new Color(0, 255, 0);
		BufferedImage out = ImageUtil.blankImage(in.getWidth(), in.getHeight(),
				Color.WHITE);
		//Graphics2D g = (Graphics2D) out.createGraphics();
		// g.drawLine(100, 100, 500, 500);
		//AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
		//g.setComposite(composite);
		for (int y = 1; y < out.getHeight() - 1; y++) {
			for (int x = 1; x < out.getWidth() - 1; x++) {
				if (in.getRGB(x, y) == background.getRGB()) {
					boolean blackFound = false;
					for (int yb = y - 1; yb < y + 2; yb++) {
						for (int xb = x - 1; xb < x + 2; xb++) {
							blackFound = blackFound || in.getRGB(xb, yb) == Color.BLACK.getRGB();
						}
					}
					if (blackFound) {
						out.setRGB(x, y, Color.BLACK.getRGB());// | 0xff000000);
					} else {
						out.setRGB(x, y, Color.WHITE.getRGB());
					}
				} else if (in.getRGB(x, y) == Color.BLACK.getRGB()) {
					out.setRGB(x, y, Color.BLACK.getRGB());
				} else {
					//g.setColor(new Color(0, 0, 0, 0));
					//g.fillRect(x, y, 1, 1);
					//out.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		ImageIO.write(out, "png", new File(outPath+File.separator+inName.replace("_outlined", "_doubleoutlined")));
	}

	public static int findBackgroundColor(BufferedImage img) {
		if (img.getRGB(0, 0) != img.getRGB(0, img.getHeight() - 1) || img.getRGB(0, 0) != img.getRGB(img.getWidth()-1, 0)
				|| img.getRGB(0, 0) != img.getRGB(img.getWidth() - 1, img.getHeight() - 1)) {
			System.out.println("This image does not have a detectable background: " + img.getRGB(0, 0) + " "
					+ img.getRGB(img.getWidth() - 1, img.getHeight() - 1) + " " + img.getRGB(img.getWidth(), 0) + " "
					+ img.getRGB(0, img.getHeight() - 1));
			throw new IllegalArgumentException("This image does not have a detectable background: " + img.getRGB(0, 0)
					+ " " + img.getRGB(img.getWidth() - 1, img.getHeight() - 1) + " " + img.getRGB(img.getWidth(), 0)
					+ " " + img.getRGB(0, img.getHeight() - 1));
		}
		return img.getRGB(0, 0);
	}

	public static double colorDiff(int rgb1, int rgb2) {
		double rd = getRed(rgb2) - getRed(rgb1);
		rd = rd * rd;
		double gd = getGreen(rgb2) - getGreen(rgb1);
		gd = gd * gd;
		double bd = getBlue(rgb2) - getBlue(rgb1);
		bd = bd * bd;
		return Math.sqrt(rd + gd + bd);
	}
}