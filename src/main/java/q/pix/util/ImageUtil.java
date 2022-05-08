package q.pix.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.RGB;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.matching.ColorDifference;
import org.color4j.colorimetry.matching.DifferenceAlgorithm;
import org.color4j.colorimetry.matching.MatchingFactory;
import org.imgscalr.Scalr;

import q.pix.colorfamily.ColorFamily;
import q.pix.colorfamily.FamilyAffinity;
import q.pix.colorfamily.ImageColorFamily;
import q.pix.colorfamily.SimilarColors;

public class ImageUtil {
	public static int IMAGE_WIDTH = 256;
	public static int IMAGE_HEIGHT = 256;
	public static int CROPPABLE_IMAGE_WIDTH = 286;
	public static int CROPPABLE_IMAGE_HEIGHT = 286;
	public static final Color GREEN_BG = new Color(0, 255, 0);
	public static final Color FAMILY_FEATURE_COLOR = new Color(220,220,0);
	public static final String FILENAME_COORDS_SPLIT = "oOo";
	public static final String FILENAME_SIZE_SPLIT = "sSs";
	private static MatchingFactory fact = MatchingFactory.getInstance();

	public static BufferedImage loadAndScale(File imageFile) {
		try {
			return downscale(ImageIO.read(imageFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage crop1To1(BufferedImage input, Point preferredCenter) {
		int size = input.getWidth() < input.getHeight() ? input.getWidth() : input.getHeight();
		int x = preferredCenter.x - (size / 2);
		int y = preferredCenter.y - (size / 2);
		x = x < 0 ? 0 : x;
		y = y < 0 ? 0 : y;
		return copyImage(input, blankImage(size, size, GREEN_BG), x, y, size);
	}

	public static BufferedImage downscaleTo(BufferedImage input, int width, int height, Point center) {
		if (input.getWidth() > width || input.getHeight() > height) {
			return Scalr.resize(input, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, width, height);
		}
		return input;
	}

	public static BufferedImage downscaleTo(BufferedImage input, int width, int height) {
		if (input.getWidth() > width || input.getHeight() > height) {
			return Scalr.resize(input, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, width, height);
		}
		return input;
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
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				output.setRGB(x, y, backgroundColor.getRGB());
			}
		}
		return output;
	}
	
	public static BufferedImage blankImageTransparent(int width, int height) {
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				output.setRGB(x, y, 0);
			}
		}
		return output;
	}

	public static BufferedImage blankImageRandom(int width, int height) {
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		SecureRandom r = new SecureRandom();
		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				output.setRGB(x, y, randomColor(r).getRGB());
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

	public static void combineImages(String leftImageDir, Optional<String> rightImageDir, boolean blanksWhereMissing,
			String outputDir) throws IOException {
		if (!rightImageDir.isPresent() && !blanksWhereMissing) {
			throw new IllegalArgumentException(
					"Either the dir containing the second set of images must exist or I must be allowed to generate blanks where they are missing");
		}
		SecureRandom rand = new SecureRandom();
		List<File> inputs = Arrays.asList(new File(leftImageDir).listFiles());
		new File(outputDir).mkdir();
		for (File input : inputs) {
			File target = null;
			if (rightImageDir.isPresent()) {
				target = new File(rightImageDir.get() + File.separator + input.getName());
				if (!target.exists() && !blanksWhereMissing) {
					continue;
				}
			}
			BufferedImage inputImage;

			if (blanksWhereMissing) {
				inputImage = ImageIO.read(input);
			} else {
				inputImage = copyIntoCenter(ImageIO.read(input),
						blankImageRandom(CROPPABLE_IMAGE_WIDTH, CROPPABLE_IMAGE_HEIGHT));
			}

			BufferedImage targetImage = null;

			if (blanksWhereMissing) {
				if (target != null && target.exists()) {
					targetImage = ImageIO.read(target);
				} else {
					targetImage = blankImageRandom(inputImage.getWidth(), inputImage.getHeight());
				}
			} else {
				targetImage = copyIntoCenter(ImageIO.read(target),
						blankImageRandom(CROPPABLE_IMAGE_WIDTH, CROPPABLE_IMAGE_HEIGHT));
			}

			int width = blanksWhereMissing ? IMAGE_WIDTH : inputImage.getWidth();
			int height = blanksWhereMissing ? IMAGE_HEIGHT : inputImage.getHeight();
			
			combineImage(inputImage, targetImage, outputDir, input.getName(), width, height);
		}
	}

	public static BufferedImage copyIntoCenter(BufferedImage in, BufferedImage out) {
		int xOffset = (out.getWidth() - in.getWidth()) / 2;
		int yOffset = (out.getHeight() - in.getHeight()) / 2;

		for (int y = 0; y < in.getWidth(); y++) {
			for (int x = 0; x < in.getWidth(); x++) {
				out.setRGB(x + xOffset, y + yOffset, in.getRGB(x, y));
			}
		}
		return out;
	}

	public static void combineImage(BufferedImage leftImage, BufferedImage rightImage, String outputDir,
			String outputNameBase, int imageWidth, int imageHeight) throws IOException {
		SecureRandom rand = new SecureRandom();
		int cols = leftImage.getWidth() > imageWidth ? leftImage.getWidth() / (imageWidth / 2) : 1;
		int rows = leftImage.getHeight() > imageHeight ? leftImage.getHeight() / (imageHeight / 2) : 1;
		
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++) {
				BufferedImage output = new BufferedImage(imageWidth * 2, imageHeight, BufferedImage.TYPE_INT_RGB);
				output.getGraphics().drawImage(leftImage, 0, 0, imageWidth, imageHeight, c * imageWidth / 2,
						r * imageHeight / 2, c * imageWidth / 2 + imageWidth, r * imageHeight / 2 + imageHeight, null);
				output.getGraphics().drawImage(rightImage, imageWidth, 0, imageWidth * 2, imageHeight,
						c * imageWidth / 2, r * imageHeight / 2, c * imageWidth / 2 + imageWidth,
						r * imageHeight / 2 + imageHeight, null);
				String namePrefix = (cols == 1 && rows == 1) ? ""
						: "_" + c * imageWidth / 2 + "_" + r * imageWidth / 2 + "_";
				backgroundToRandom(output, rand);
				ImageIO.write(output, "png", new File(outputDir + File.separator + namePrefix + outputNameBase));
			}
		}

	}

	public static BufferedImage copyImage(BufferedImage input) {
		return copyImage(input, 0, 0);
	}

	public static BufferedImage copyImage(BufferedImage input, int xOffset, int yOffset) {
		BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
		return copyImage(input, output, xOffset, yOffset, false);
	}

	public static BufferedImage copyImage(BufferedImage input, BufferedImage output, int xOffset, int yOffset,
			boolean cropBorder) {
		// Skip a 4 pixel border because p2p has problems in certain cases because those
		// pixels can't benefit from neighbors
		int modifier = cropBorder ? 3 : 0;
		for (int x = modifier; x < input.getWidth() - modifier; x++) {
			for (int y = modifier; y < input.getHeight() - modifier; y++) {
				if (x < input.getWidth() && y < input.getHeight()) {
					output.setRGB(x + xOffset, y + yOffset, input.getRGB(x, y));
				} else {
					output.setRGB(x + xOffset, y + yOffset, GREEN_BG.getRGB());
				}
			}
		}
		return output;

	}

	public static BufferedImage copyImage(BufferedImage input, BufferedImage output, int xOffset, int yOffset) {
		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				if (x + xOffset < output.getWidth() && y + yOffset < output.getHeight()) {
					output.setRGB(x + xOffset, y + yOffset, input.getRGB(x, y));
				} 
			}
		}
		return output;

	}
	
	/**
	 * Crops, centered around the center
	 * 
	 * @param input
	 * @param out
	 * @return out, though it is modified in place
	 */

	public static BufferedImage cropTo(BufferedImage input, BufferedImage out) {
		int xOffset = (input.getWidth() - out.getWidth()) / 2;
		int yOffset = (input.getHeight() - out.getHeight()) / 2;
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				int inX = x + xOffset;
				int inY = y + yOffset;
				if (inX >= 0 && inX < input.getWidth() && inY >= 0 && inY < input.getHeight()) {
					out.setRGB(x, y, input.getRGB(inX, inY));
				}
			}
		}
		return out;
	}

	public static BufferedImage copyImage(BufferedImage input, BufferedImage output, int fromX, int fromY, int size) {
		for (int x = fromX; x < size; x++) {
			for (int y = fromY; y < size; y++) {
				output.setRGB(x, y, input.getRGB(x, y));
			}
		}
		return output;

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
		combineImages(dir, Optional.of(dir), true, FileUtil.inputToTestsetOutputDir(dir));
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

	/**
	 * Splits out each sprite in the images in the input dir into its own file
	 */
	public static void splitSprites(File inputDir) throws IOException {
		File[] files = inputDir.listFiles();
		File outDir = new File(inputDir.getAbsolutePath() + File.separator + "splitOut");
		File smallOutputDir = new File(inputDir.getAbsolutePath() + File.separator + "splitOut_small");
		File largeOutputDir = new File(inputDir.getAbsolutePath() + File.separator + "splitOut_big");
		outDir.mkdir();
		smallOutputDir.mkdir();
		largeOutputDir.mkdir();

		for (File file : files) {
			if (file.getName().endsWith("png")) {
				splitSpriteFile(file, outDir.getAbsolutePath());
			}
		}
	}

	/**
	 * Splits out each sprite in the image into its own file
	 */
	public static void splitSpriteFile(File inputFile, String outputDir) throws IOException {
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

							String outFileName = inputFile.getName().substring(0, inputFile.getName().indexOf('.'))
									.replaceAll(" ", "_") + "split_" + imageCt + ".png";

							if (subImage.getWidth() <= 128 && subImage.getHeight() <= 128) {
								ImageIO.write(centerImageOnGreen(subImage, 128), "png",
										new File(outputDir + "_small" + File.separator + outFileName));
							} else if (subImage.getWidth() <= 256 && subImage.getHeight() <= 256) {
								ImageIO.write(centerImageOnGreen(subImage, 256), "png",
										new File(outputDir + File.separator + outFileName));
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

	}

	public static void sliceImages(File inputDir, File outputDir) throws IOException {
		for (File f : inputDir.listFiles()) {
			BufferedImage input = ImageIO.read(f);
			// sliceImage(input, f.getName(), outputDir);
			sliceImageRandomly(input, f.getName(), outputDir);
		}
	}

	public static void sliceImagePairs(File parentDir) throws IOException {
		for (File f : new File(parentDir.getAbsolutePath() + File.separator + "input_orig").listFiles()) {
			BufferedImage input = ImageIO.read(f);
			File targetF = new File(
					parentDir.getAbsoluteFile() + File.separator + "target_orig" + File.separator + f.getName());
			BufferedImage target = ImageIO.read(targetF);
			// sliceImage(input, f.getName(), outputDir);
			sliceImagePairRandomly(input, target, f.getName(), parentDir);
		}
	}

	/**
	 * Splits an image into sub-images.
	 * 
	 * @param input
	 * @param width
	 * @param height
	 * @param doBorder
	 */
	public static void sliceImage(BufferedImage input, String inputName, File outputDir) throws IOException {

		int y = 0;
		while (y < input.getHeight()) {
			int x = 0;
			int yOffset = y < 0 ? -y : 0;
			int workingY = y < 0 ? 0 : y;
			while (x < input.getWidth()) {
				int workingX = x < 0 ? 0 : x;
				int xOffset = x < 0 ? -x : 0;

				int w = (workingX + IMAGE_WIDTH) < input.getWidth() ? IMAGE_WIDTH
						: IMAGE_WIDTH - ((workingX + IMAGE_WIDTH) - input.getWidth());
				int h = (workingY + IMAGE_HEIGHT) < input.getHeight() ? IMAGE_HEIGHT
						: IMAGE_HEIGHT - ((workingY + IMAGE_HEIGHT) - input.getHeight());
				w = x < 0 ? w + x : w;
				h = y < 0 ? h + y : h;

				copySubImage(input, inputName, outputDir, workingX, workingY, w, h, xOffset, yOffset, x, y);
				x += IMAGE_WIDTH;
			}
			y += IMAGE_HEIGHT;
		}
	}

	public static void copySubImage(BufferedImage input, String inputName, File outputDir, int workingX, int workingY,
			int w, int h, int xOffset, int yOffset, int x, int y) throws IOException {
		BufferedImage slice = blankImage(GREEN_BG);

		copyImage(input.getSubimage(workingX, workingY, w, h), slice, xOffset, yOffset, false);
		// addBorder(input, slice, x, y, w, h);
		ImageIO.write(slice, "png",
				new File(outputDir + File.separator + inputName.replace(".png", FILENAME_COORDS_SPLIT + x + "_" + y
						+ "_" + FILENAME_SIZE_SPLIT + input.getWidth() + "_" + input.getHeight() + ".png")));
	}

	public static void sliceImageRandomly(BufferedImage input, String inputName, File outputDir) throws IOException {

		Set<String> coordsUnpainted = new HashSet<>();
		for (int y = 0; y < input.getHeight(); y++) {
			for (int x = 0; x < input.getHeight(); x++) {
				coordsUnpainted.add(x + "," + y);
			}
		}

		Random r = new Random();
		while (!coordsUnpainted.isEmpty()) {
			int x = r.nextInt(input.getWidth() + fudgeFactorWidth()) - fudgeFactorWidth();
			int y = r.nextInt(input.getHeight() + fudgeFactorHeight()) - fudgeFactorHeight();
			int yOffset = y < 0 ? -y : 0;
			int workingY = y < 0 ? 0 : y;
			int workingX = x < 0 ? 0 : x;
			int xOffset = x < 0 ? -x : 0;
			int w = (workingX + IMAGE_WIDTH) < input.getWidth() ? IMAGE_WIDTH
					: IMAGE_WIDTH - ((workingX + IMAGE_WIDTH) - input.getWidth());
			int h = (workingY + IMAGE_HEIGHT) < input.getHeight() ? IMAGE_HEIGHT
					: IMAGE_HEIGHT - ((workingY + IMAGE_HEIGHT) - input.getHeight());
			w = x < 0 ? w + x : w;
			h = y < 0 ? h + y : h;

			for (int sx = 0; sx < w; sx++) {
				for (int sy = 0; sy < h; sy++) {
					coordsUnpainted.remove((sx + workingX) + "," + (sy + workingY));
				}
			}
			copySubImage(input, inputName, outputDir, workingX, workingY, w, h, xOffset, yOffset, x, y);

		}
	}

	/**
	 * When splitting an image up randomly we'll want to be able to go somewhat
	 * outside the bounds of the base image or else the edges will always be on the
	 * outer parts of the split image. The fudge factor is how much outside the
	 * bounds of the image we'll randomly go
	 * 
	 * The division is to get the ratio to apply to the actual image sizes
	 * 
	 * @return the fudge factor
	 */
	private static int fudgeFactorWidth() {
		return (int) ((256.0 / 200.0) * IMAGE_WIDTH);
	}

	private static int fudgeFactorHeight() {
		return (int) ((256.0 / 200.0) * IMAGE_HEIGHT);
	}

	/*
	 * public static void sliceImagePairRandomly(BufferedImage input, BufferedImage
	 * target, String inputName, File outputDirParent) throws IOException {
	 * sliceImagePairRandomly(input, target, inputName, outputDirParent); }
	 */

	public static void sliceImagePairsSmall(File parentDir) throws IOException {
		File splitDir = new File(parentDir.getAbsolutePath() + File.separator + "split");
		if (!splitDir.exists()) {
			splitDir.mkdir();
		}
		File toInputDir = new File(parentDir.getAbsolutePath() + File.separator + "split" + File.separator + "input");
		File toTargetDir = new File(parentDir.getAbsolutePath() + File.separator + "split" + File.separator + "target");
		if (!toInputDir.exists()) {
			toInputDir.mkdir();
		}
		if (!toTargetDir.exists()) {
			toTargetDir.mkdir();
		}
		for (File f : new File(parentDir.getAbsolutePath() + File.separator + "input_orig").listFiles()) {
			BufferedImage input = ImageIO.read(f);
			File targetF = new File(f.getAbsolutePath().replace("input_orig", "target_orig"));
			//File targetF = new File(
				//	parentDir.getAbsoluteFile() + File.separator + "target_orig" + File.separator + f.getName());
			BufferedImage target = ImageIO.read(targetF);

			sliceImagePairMethodicallySmall(input, target, f.getName(), toInputDir, toTargetDir);
		}
		System.out.println("Done slicing");
	}

	public static void sliceTestsetSmall(File parentDir) throws IOException {

		File testsetInputDir = new File(parentDir.getAbsolutePath() + File.separator + "testset_input");
		if (!testsetInputDir.exists()) {
			testsetInputDir.mkdir();
		}
		File testsetTargetDir = new File(parentDir.getAbsolutePath() + File.separator + "testset_output");
		if (!testsetTargetDir.exists()) {
			testsetTargetDir.mkdir();
		}
		for (File f : new File(parentDir.getAbsolutePath() + File.separator + "input_orig").listFiles()) {
			BufferedImage input = ImageIO.read(f);
			BufferedImage blankTarget = blankImage(input.getWidth(), input.getHeight(), GREEN_BG);
			sliceImagePairMethodicallySmall(blankTarget, input, f.getName(), testsetInputDir, testsetTargetDir);
		}
	}

	public static void subsetFiles(File parentDir) throws IOException {
		File[] contents = parentDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("png");
			}
		});
		SecureRandom rand = new SecureRandom();
		File aDir = new File(parentDir.getParent() + File.separator + "splitA");
		aDir.mkdir();
		File bDir = new File(parentDir.getParent() + File.separator + "splitB");
		bDir.mkdir();

		for (File f : contents) {
			Path originalPath = f.toPath();
			Path copyTo;
			if (rand.nextBoolean()) {
				copyTo = Paths.get(aDir.getAbsolutePath() + File.separator + f.getName());
			} else {
				copyTo = Paths.get(bDir.getAbsolutePath() + File.separator + f.getName());
			}
			Files.copy(originalPath, copyTo, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Finds the upper left (index 0) and lower right (index 1) bounds of the image
	 * to make the smallest bounding box
	 * 
	 * @param img The image
	 * @return Optional of upper left and lower right bounds. If optional absent,
	 *         the whole image is background.
	 */
	public static Optional<Point[]> getBounds(BufferedImage img) {
		Point[] bounds = new Point[2];
		int top = 0;
		top = getTopBound(img);
		if (top < 0) {
			return Optional.empty();
		}
		bounds[0] = new Point(getLeftBound(img), top);
		bounds[1] = new Point(getRightBound(img), getBottomBound(img));
		return Optional.of(bounds);
	}

	private static int getTopBound(BufferedImage img) {
		int background = new Color(0, 255, 0).getRGB();
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if (background != img.getRGB(x, y)) {
					return y;
				}
			}
		}
		return -1; // whole image is background
	}

	// Technically 1 higher than the actual bound, as the width is in base 1 and we
	// need index
	private static int getBottomBound(BufferedImage img) {
		int background = new Color(0, 255, 0).getRGB();
		for (int y = img.getHeight() - 1; y > -1; y--) {
			for (int x = 0; x < img.getWidth() - 1; x++) {
				if (background != img.getRGB(x, y)) {
					return y;
				}
			}
		}
		return -1; // whole image is background
	}

	private static int getLeftBound(BufferedImage img) {
		int background = new Color(0, 255, 0).getRGB();
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				if (background != img.getRGB(x, y)) {
					return x;
				}
			}
		}
		return -1; // whole image is background
	}

	// Technically 1 higher than the actual bound, as the width is in base 1 and we
	// need index
	private static int getRightBound(BufferedImage img) {
		int background = new Color(0, 255, 0).getRGB();
		for (int x = img.getWidth() - 1; x > -1; x--) {
			for (int y = img.getHeight() - 1; y > -1; y--) {
				if (background != img.getRGB(x, y)) {
					return x;
				}
			}
		}
		return -1; // whole image is background
	}

	
	/** I'm putting this one on hold for the moment because I don't think it's a route I want to go
	 * But the idea was that the slicing should try to factor the amount of content in the slice placement
	 * so I don't get a bunch of slices that are just a bit of foot
	public static void sliceImagePairMaximizingContent(BufferedImage input, BufferedImage target, String inputName,
			File inputToDir, File targetToDir) throws IOException {
		BufferedImage tracker = target.getSubimage(0, 0, target.getWidth(), target.getHeight());
		Optional<Point[]> bounds = getBounds(input);
		
		while(bounds.isPresent()) {
			BufferedImage topSub = tracker.getSubimage(0, bounds.get()[0].y, tracker.getWidth(), IMAGE_HEIGHT);
			Optional<Point[]> subBounds = getBounds(topSub);
			if(subBounds.isPresent()) {
				cxcxxccxs
			}
			
			bounds = getBounds(input);
		}
		
	}*/
	
	public static void sliceImagePairMethodicallySmall(BufferedImage input, BufferedImage target, String inputName,
			File inputToDir, File targetToDir) throws IOException {

		Optional<Point[]> bounds = getBounds(input);
		if (bounds.isEmpty()) {
			return;//
		}
		int y = bounds.get()[0].y;
		do {
			int x = bounds.get()[0].x;
			do {
				int width = x + IMAGE_WIDTH > input.getWidth() ? input.getWidth() - x : IMAGE_WIDTH;
				int height = y + IMAGE_HEIGHT > input.getHeight() ? input.getHeight() - y : IMAGE_HEIGHT;

				BufferedImage slice = input.getSubimage(x, y, width, height);
				BufferedImage outFile = blankImage(IMAGE_WIDTH, IMAGE_HEIGHT, GREEN_BG);
				copyImage(slice, outFile, 0, 0);
				if (!isBackgroundColorImage(outFile)) {
					ImageIO.write(outFile, "png",
							new File(inputToDir + File.separator
									+ inputName.replace(".png",
											FILENAME_COORDS_SPLIT + x + "_" + y + "_" + FILENAME_SIZE_SPLIT
													+ input.getWidth() + "_" + input.getHeight() + ".png")));

					slice = target.getSubimage(x, y, width, height);
					outFile = blankImage(IMAGE_WIDTH, IMAGE_HEIGHT, GREEN_BG);
					copyImage(slice, outFile, 0, 0);

					ImageIO.write(outFile, "png",
							new File(targetToDir + File.separator
									+ inputName.replace(".png",
											FILENAME_COORDS_SPLIT + x + "_" + y + "_" + FILENAME_SIZE_SPLIT
													+ input.getWidth() + "_" + input.getHeight() + ".png")));
				}
				x += (int) (IMAGE_WIDTH * .75);
			} while  (x  < bounds.get()[1].x);
			y += (int) (IMAGE_HEIGHT * .75);
		} while(y < bounds.get()[1].y);

	}

	/*
	 * extracted from sliceImagePairMethodicallySmall but if
	 * (!exceedsMaxAcceptableBackgroundColorPercent(inputSlice, 25)) { BufferedImage
	 * combined = blankImage(IMAGE_HEIGHT * 2, size, GREEN_BG);
	 * copyImage(inputSlice, combined, 0, 0, false); copyImage(target.getSubimage(x,
	 * y, size, size), combined, size, 0, false); ImageIO.write(combined, "png", new
	 * File(outputDirParent + File.separator + inputName.replace(".png",
	 * FILENAME_COORDS_SPLIT + x + "_" + y + "_" + FILENAME_SIZE_SPLIT +
	 * inputSlice.getWidth() + "_" + inputSlice.getHeight() + ".png"))); }
	 */

	public static void sliceImagePairRandomly(BufferedImage input, BufferedImage target, String inputName,
			File outputDirParent) throws IOException {
		long t = System.currentTimeMillis();
		File inputSliceOutputDir = new File(outputDirParent.getAbsolutePath() + File.separator + "input");
		File targetSliceOutputDir = new File(outputDirParent.getAbsolutePath() + File.separator + "target");
		if (!inputSliceOutputDir.exists()) {
			inputSliceOutputDir.mkdir();
		}
		if (!targetSliceOutputDir.exists()) {
			targetSliceOutputDir.mkdir();
		}

		if (input.getWidth() != target.getWidth() || input.getHeight() != target.getHeight()) {
			throw new IllegalArgumentException(
					"Input 1 and input 2 of input name " + inputName + " must be the same dimension");
		}

		Set<String> coordsUnpainted = new HashSet<>(input.getWidth() * input.getHeight());
		for (int y = 0; y < input.getHeight(); y++) {
			for (int x = 0; x < input.getWidth(); x++) {
				coordsUnpainted.add(x + "," + y);
			}
		}

		Random r = new Random();
		int ct = 0;

		while (!coordsUnpainted.isEmpty()) {
			int x = r.nextInt(fudgeFactorWidth());
			int y = r.nextInt(fudgeFactorHeight());
			int yOffset = y < 0 ? -y : 0;
			int workingY = y < 0 ? 0 : y;
			int workingX = x < 0 ? 0 : x;
			int xOffset = x < 0 ? -x : 0;
			int w = (workingX + IMAGE_WIDTH) < input.getWidth() ? IMAGE_WIDTH
					: IMAGE_WIDTH - ((workingX + IMAGE_WIDTH) - input.getWidth());
			int h = (workingY + IMAGE_HEIGHT) < input.getHeight() ? IMAGE_HEIGHT
					: IMAGE_HEIGHT - ((workingY + IMAGE_HEIGHT) - input.getHeight());
			w = x < 0 ? w + x : w;
			h = y < 0 ? h + y : h;

			boolean nonBackgroundFound = false;
			for (int sx = 0; sx < w; sx++) {
				for (int sy = 0; sy < h; sy++) {
					int coordX = (sx + workingX);
					int coordY = (sy + workingY);
					if (coordX >= 0 && coordX < input.getWidth() && coordY >= 0 && coordY < input.getHeight()) {
						if (input.getRGB(coordX, coordY) != GREEN_BG.getRGB()) {
							nonBackgroundFound = true;
						}
					}
					coordsUnpainted.remove(coordX + "," + coordY);
				}
			}
			// Include a percentage of the all-green inputs so there are some examples of
			// them
			if (nonBackgroundFound == false) {
				if (r.nextInt(100) < 10) {
					nonBackgroundFound = true;
				}
			}

			if (nonBackgroundFound) {
				copySubImage(input, inputName, inputSliceOutputDir, workingX, workingY, w, h, xOffset, yOffset, x, y);
				copySubImage(target, inputName, targetSliceOutputDir, workingX, workingY, w, h, xOffset, yOffset, x, y);
			}
			ct++;
		}
		long timeTaken = System.currentTimeMillis() - t;
		System.out.println("Per image: " + (timeTaken / ct));
	}

	/*
	 * public static void addBorder(BufferedImage bigImage, BufferedImage target,
	 * int x, int y, int w, int h) { BufferedImage downscaled =
	 * downscaleTo(crop1To1(bigImage, new Point(w, h)), 100, 100); // need to go do
	 * the math // but this is for // 256x256
	 * 
	 * doBorder(downscaled, target); }
	 */
	public static void combineImages(File inputDir, File outputDir) throws IOException {
		File[] inputs = inputDir.listFiles();
		Arrays.sort(inputs);
		try {
			String baseName = inputs[0].getName().substring(0, inputs[0].getName().indexOf(FILENAME_COORDS_SPLIT));

			List<File> imgParts = new ArrayList<>();
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i].getName().endsWith("-inputs.png") || inputs[i].getName().endsWith("-targets.png")) {
					continue;
				}
				if (inputs[i].getName().startsWith(baseName)) {
					imgParts.add(inputs[i]);
				} else {
					Point size = parseSize(imgParts.get(0).getName());
					BufferedImage combined = reconstructSlicedImage(imgParts, size.x, size.y);
					ImageIO.write(combined, "png", new File(outputDir + File.separator + baseName + "_combined.png"));

					baseName = inputs[i].getName().substring(0, inputs[i].getName().indexOf(FILENAME_COORDS_SPLIT));
					imgParts = new ArrayList<>();
					imgParts.add(inputs[i]);
				}
			}

			Point size = parseSize(imgParts.get(0).getName());
			BufferedImage combined = reconstructSlicedImage(imgParts, size.x, size.y);
			ImageIO.write(combined, "png", new File(outputDir + File.separator + baseName + "_combined.png"));
		} catch (Exception e) {
			System.err.println("Error file: " + inputs[0].getName());
			e.printStackTrace();
			throw e;
		}
	}

	public static BufferedImage reconstructSlicedImage(List<File> inputFiles, int origWidth, int origHeight)
			throws IOException {
		/*List<Point> coords = inputFiles.stream().map(f -> parseCoords(f.getName())).collect(Collectors.toList());
		int minX = min(coords, ImageUtil::getX);
		int minY = min(coords, ImageUtil::getY);
		int maxX = max(coords, ImageUtil::getX);
		int maxY = max(coords, ImageUtil::getY);
*/
		//int width = maxX - minX + origWidth;
		//int height = maxY - minY + origHeight;
		//width = width < origWidth ? origWidth : width;
		//height = height < origHeight ? origHeight : height;
		BufferedImage combined = blankImage(origWidth, origHeight, GREEN_BG);
		
		for (int i = 0; i < inputFiles.size(); i++) {
			Point coords = parseCoords(inputFiles.get(i).getName());
			copyImage(ImageIO.read(inputFiles.get(i)), combined, coords.x, coords.y);// coords.get(i).x - minX, coords.get(i).y - minY, true);
			
		}

		try {
			//return combined.getSubimage(-minX, -minY, origWidth, origHeight);
			//return combined.getSubimage(minX, minY, origWidth, origHeight);
			return combined;
		} catch(Exception e) {
			System.out.println("Failed on "+inputFiles.get(0).getName());
			throw e;
		}
	}

	public static int min(List<Point> points, Function<Point, Integer> getter) {
		int v = getter.apply(points.get(0));
		for (Point p : points) {
			v = getter.apply(p) < v ? getter.apply(p) : v;
		}
		return v;
	}

	public static int max(List<Point> points, Function<Point, Integer> getter) {
		int v = getter.apply(points.get(0));
		for (Point p : points) {
			v = getter.apply(p) > v ? getter.apply(p) : v;
		}
		return v;
	}

	public static int getX(Point p) {
		return p.x;
	}

	public static int getY(Point p) {
		return p.y;
	}

	public static Point parseCoords(String name) {
		String coordsPart = name.substring(name.indexOf(FILENAME_COORDS_SPLIT) + FILENAME_COORDS_SPLIT.length(),
				name.indexOf(FILENAME_SIZE_SPLIT));
		return parseIntsFromFilename(coordsPart);
	}

	public static Point parseSize(String name) {
		String sizePart = name.substring(name.indexOf(FILENAME_SIZE_SPLIT) + FILENAME_SIZE_SPLIT.length(),
				name.indexOf(".png"));

		return parseIntsFromFilename(sizePart);
	}

	public static Point parseIntsFromFilename(String part) {
		if (part.endsWith("-outputs")) {
			part = part.substring(0, part.indexOf("-outputs"));
		}
		return new Point(Integer.parseInt(part.split("_")[0]), Integer.parseInt(part.split("_")[1]));
	}

//	public static void doBorder(BufferedImage source, BufferedImage destination) {
//		Point destXYDir = new Point(10,0);
//		int destX = 0;
//		int destY = 0;
//		int y=0;
//		while(y < source.getHeight()) {
//			int x = 0;
//			while(x < source.getWidth()) {
//				BufferedImage subImage = source.getSubimage(x,y,x+10, y+10);
//				copyImage(subImage, destination, destX, destY);
//				destX+=destXYDir.x;
//				destY+=destXYDir.y;
//				
//				destX = destX > 236 ? 226 : destX; // bottom right
//				destX = destX < 0 ? 0 : destX; // bottom left
//				
//				x+=10;
//			}
//			y+=10;
//		}
//
//	}
	/*
	 * Nobody calls this it seems. Prep for deletion. public static void
	 * doBorder(BufferedImage borderSource, BufferedImage destination) { int frameCt
	 * = 0; int sourceY = 0; int iteration = 0; while (sourceY <
	 * borderSource.getHeight() - 9) { int sourceX = 0; while (sourceX <
	 * borderSource.getWidth() - 9) { BufferedImage subImage =
	 * borderSource.getSubimage(sourceX, sourceY, 10, 10);
	 * 
	 * Point dest = getXYCoords(iteration); System.out.println("To dest " +
	 * iteration + " " + dest + " against source " + sourceX + " ," + sourceY);
	 * copyImage(subImage, destination, dest.x, dest.y, false);
	 * 
	 * sourceX += 10; sourceX = frameCt % 7 == 0 ? sourceX + 1 : sourceX;
	 * iteration++; } sourceY += 10; sourceY = frameCt % 7 == 0 ? sourceY + 1 :
	 * sourceY; }
	 * 
	 * }
	 */
	// I can fit 25 on the top row, then 24 on the vertical down
	// then another 24 going right to left along the bottom
	// then 23 going up the left for a total of 96 10x10 so I skip 1 pixel on the
	// source
	// every 7 images - enh. maybe I won't bother skipping.
	/*
	 * Only called by addBorder, which is not used, so I'm commenting this code tree
	 * out in prep for deletion private static Point getXYCoords(int iteration) {
	 * int topRowIterations = 256 / 10; int rightVerticalIterations = 246 / 10; int
	 * bottomRowIterations = 246 / 10; int leftVerticalIterations = 236 / 10;
	 * 
	 * if (iteration < topRowIterations) { System.out.println("Iteration " +
	 * iteration + " in first"); return new Point(10 * iteration, 0); } if
	 * (iteration < topRowIterations + rightVerticalIterations) {
	 * System.out.println("Iteration " + iteration + " in second"); return new
	 * Point(245, (iteration - topRowIterations) * 10); }
	 * 
	 * if (iteration < topRowIterations + rightVerticalIterations +
	 * bottomRowIterations) { int numInto = iteration - topRowIterations -
	 * rightVerticalIterations; System.out.println("Iteration " + iteration +
	 * " in third " + numInto); return new Point(245 - (10 * numInto), 245); }
	 * System.out.println("Iteration " + iteration + " in fourth"); int numInto =
	 * iteration - topRowIterations - rightVerticalIterations - bottomRowIterations;
	 * return new Point(0, 245 - (numInto * 10));
	 * 
	 * }
	 */


	/**
	 * Does the same thing as paint to family but does every combination of the
	 * first 3 groups, and then all the combinations of any remaining groups
	 * 
	 * @param baseColors
	 * @param inputFile
	 * @param family
	 * @param outDir
	 * @throws IOException
	 */
//	public static void paintToFamilyColorIteration(List<Color> baseColors, File inputFile, ColorFamily family, File outDir, String outputSuffux)
//			throws IOException {
//
//		for(List<Color> colorCombo :  generateColorCombos(baseColors, family)) {
//			BufferedImage painted = paintToFamily(colorCombo, ImageIO.read(inputFile), family);
//			ImageIO.write(painted, "png", new File(outDir.getAbsoluteFile() + File.separator + inputFile.getName().replace(".png", ".png")));
//		}
//		
//	}

	public static List<List<Color>> generateColorCombos(List<Color> baseColors, ColorFamily family) {
		int mainMaxIdx = 1;
		for (int i = 0; i < family.getColorGroups().size(); i++) {
			if (family.get(i).size() == 0 || i > 3) {
				mainMaxIdx = i;
				break;
			}
		}
		int secondaryMaxIdx = -1;
		for (int i = mainMaxIdx; i < family.getColorGroups().size(); i++) {
			if (family.get(i).size() == 0) {
				secondaryMaxIdx = -1;
				break;
			}
			if (i > mainMaxIdx + 2) {
				secondaryMaxIdx = i;
				break;
			}
		}
		List<List<Color>> colorCombos = new ArrayList<>();

		for (int i = 0; i < mainMaxIdx; i++) {
			List<Color> colorDest = new ArrayList<>();
			getColorWrapping(baseColors, 0, i, mainMaxIdx, mainMaxIdx, colorDest);
			for (int z = mainMaxIdx; z < secondaryMaxIdx; z++) {
				getColorWrapping(baseColors, mainMaxIdx, z, secondaryMaxIdx, secondaryMaxIdx, colorDest);
			}
			int continueFrom = secondaryMaxIdx < 0 ? mainMaxIdx : secondaryMaxIdx;
			for (int r = continueFrom + 1; r < baseColors.size(); r++) {
				colorDest.add(baseColors.get(r));
			}
			colorCombos.add(colorDest);
		}

		return colorCombos;

	}

	private static void getColorWrapping(List<Color> colors, int returnTo, int start, int max, int goalSize,
			List<Color> destination) {
		int idx = start;
		while (destination.size() < goalSize) {
			if (idx >= max) {
				idx = returnTo;
			}
			destination.add(colors.get(idx));
			idx++;
		}

	}

	public static void paintToFamily(List<Color> baseColors, File inputFile, ColorFamily family, File outDir,
			String outputSuffix) throws IOException {
		BufferedImage painted = paintToFamily(baseColors, ImageIO.read(inputFile), family, inputFile.getName());
		ImageIO.write(painted, "png", new File(outDir.getAbsoluteFile() + File.separator
				+ inputFile.getName().replace(".png", outputSuffix + ".png")));
	}

	public static BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {
		double rads = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
		int w = img.getWidth();
		int h = img.getHeight();
		int newWidth = (int) Math.floor(w * cos + h * sin);
		int newHeight = (int) Math.floor(h * cos + w * sin);

		BufferedImage rotated = blankImage(newWidth, newHeight, GREEN_BG);
		Graphics2D g2d = rotated.createGraphics();
		AffineTransform at = new AffineTransform();
		at.translate((newWidth - w) / 2, (newHeight - h) / 2);

		int x = w / 2;
		int y = h / 2;

		at.rotate(rads, x, y);
		g2d.setTransform(at);
		g2d.drawImage(img, 0, 0, null);
		g2d.setColor(GREEN_BG);
		g2d.drawRect(0, 0, newWidth - 1, newHeight - 1);
		g2d.dispose();
		BufferedImage out = blankImage(IMAGE_WIDTH, IMAGE_HEIGHT, GREEN_BG);
		// out = copyImage(rotated, out, -(rotated.getWidth()-out.getWidth())/2,
		// -(rotated.getHeight()-out.getHeight())/2, false);//copyImage(rotated, ,
		// (newWidth-img.getWidth()/2), (newHeight-img.getHeight()/2),img.getHeight());
		return cropTo(rotated, out);
	}

	private static final int ROTATION_SIZE = 90;

	/**
	 * Repeats the paintToFamily over a series of rotations to help prevent
	 * overfitting. Match with paintInputRotation
	 * 
	 * @param baseColors
	 * @param inputFile
	 * @param family
	 * @param outDir
	 * @param outputSuffix
	 * @throws IOException
	 */
	public static void paintToFamilyRotation(List<Color> baseColors, File inputFile, ColorFamily family, File outDir,
			String outputSuffix) throws IOException {
		BufferedImage image = ImageIO.read(inputFile);
		BufferedImage painted = paintToFamily(baseColors, image, family, inputFile.getName());
		for (int rot = 0; rot < 360; rot += ROTATION_SIZE) {
			ImageIO.write(rotateImageByDegrees(painted, rot), "png", new File(outDir.getAbsoluteFile() + File.separator
					+ inputFile.getName().replace(".png", outputSuffix + rot + ".png")));
		}
	}

	public static void paintInput(List<Color> baseColors, File inputFile, ColorFamily family, File outDir,
			String outputSuffix) throws IOException {
		BufferedImage painted = paintInput(baseColors, ImageIO.read(inputFile), family);
		ImageIO.write(painted, "png", new File(outDir.getAbsoluteFile() + File.separator
				+ inputFile.getName().replace(".png", outputSuffix + ".png")));
	}

	/**
	 * Repeats the paintInput over a series of rotations to help prevent
	 * overfitting. Match with paintToFamilyRotation
	 * 
	 * @param baseColors
	 * @param inputFile
	 * @param family
	 * @param outDir
	 * @param outputSuffix
	 * @throws IOException
	 */
	public static void paintInputRotation(List<Color> baseColors, File inputFile, ColorFamily family, File outDir,
			String outputSuffix) throws IOException {
		BufferedImage painted = paintInput(baseColors, ImageIO.read(inputFile), family);
		for (int rot = 0; rot < 360; rot += ROTATION_SIZE) {
			ImageIO.write(rotateImageByDegrees(painted, rot), "png", new File(outDir.getAbsoluteFile() + File.separator
					+ inputFile.getName().replace(".png", outputSuffix + rot + ".png")));
		}
	}

	// Not using analyze colors right now because LAB still ends up grouping some
	// colors together in ways I don't like
	public static void analyzeColors(File inputFile) throws IOException {
		BufferedImage analyzed = analyzeColors(ImageIO.read(inputFile));
		ImageIO.write(analyzed, "png", new File(inputFile.getAbsolutePath().replace(".png", "_colors.png")));
	}

	public static SortedSet<Color> getDistinctColors(BufferedImage img) {
		SortedSet<Color> colorsFound = new TreeSet<>();
		// Color inputBackground = new Color(img.getRGB(0, 0));
		Color inputBackground = new Color(0, 255, 0);
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Color c = new Color(img.getRGB(x, y));
				if (c.getAlpha() == 0 || c.equals(inputBackground)) {
					continue;
				}
				if (!colorsFound.contains(c)) {
					colorsFound.add(c);
				}
			}
		}
		return colorsFound;
	}

	public static int[] findColor(Color c, BufferedImage img) {
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Color ic = new Color(img.getRGB(x, y));
				if (ic.equals(c)) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	public static FamilyAffinity findClosestColorFamily(BufferedImage img, List<ColorFamily> colorFamilies) {
		return findClosestColorFamily(ImageUtil.getDistinctColors(img), colorFamilies);
	}

	public static FamilyAffinity findClosestColorFamily(Set<Color> imgColors, List<ColorFamily> colorFamilies) {
		FamilyAffinity closestAffinity = null;
		for (ColorFamily family : colorFamilies) {
			FamilyAffinity aff = new FamilyAffinity(imgColors, family);

			if (closestAffinity == null) {
				closestAffinity = aff;
			} else {
				FamilyAffinity[] compare = new FamilyAffinity[] { closestAffinity, aff };
				Arrays.sort(compare);
				closestAffinity = compare[0];
			}
		}
		return closestAffinity;
	}

	// Using LAB .... I'm not using this right now but I might
	public static BufferedImage analyzeColors(BufferedImage input) {
		BufferedImage scaled = input;

		Set<Color> colorsFound = new HashSet<>();
		Set<SimilarColors> similars = new HashSet<>();
		Set<Color> lonelies = new HashSet<>();
		Color inputBackground = new Color(input.getRGB(0, 0));
		for (int x = 0; x < scaled.getWidth(); x++) {
			for (int y = 0; y < scaled.getHeight(); y++) {
				Color c = new Color(scaled.getRGB(x, y));
				if (c.getAlpha() == 0 || c.equals(inputBackground)) {
					continue;
				}
				if (!colorsFound.contains(c)) {
					colorsFound.add(c);
				}
			}
		}

		// Not using this output right now... This just shows all of the
		// colors in the image
		int size = ((int) Math.sqrt(colorsFound.size())) + 1;
		BufferedImage output = blankImage(size, size, GREEN_BG);

		for (Color c : colorsFound) {

			Optional<SimilarColors> sc = findMostSimilar(c, colorsFound, similars);
			if (sc.isEmpty()) {
				lonelies.add(c);
			} else {
				similars.add(sc.get());
			}
		}
		ColorFamily family = new ColorFamily();
		for (SimilarColors sc : similars) {
			if (!family.isInFamily(sc.getC1())) {
				Set<Color> group = makeColorGroup(sc.getC1(), similars);
				group.addAll(makeColorGroup(sc.getC2(), similars));
				family.addGroup(group);
			}

		}

		for (Set<Color> group : family.getColorGroups()) {
			System.out.println("FAMILY");
			for (Color c : group) {
				System.out.println(c.getRed() + " " + c.getGreen() + " " + c.getBlue());
			}
		}
		output = blankImage(input.getWidth(), input.getHeight(), GREEN_BG);
		paintColorFamilies(input, output, family);
		return output;
	}
	
	
	/**
	 * Takes the directories of inputs and outputs and uses the inputs to mask away everything
	 * except the family feature output
	 * @param inputsDir Input files that correspond to the outputs
	 * @param shadedOutputsDir The outputs from p2p
	 */
	public static void cleanColorFamilyOutput(File inputsDir, File shadedOutputsDir) throws IOException {
		File cleanOutputDir = new File(shadedOutputsDir.getAbsolutePath()+"_cleaned");
		cleanOutputDir.mkdir();
		
		Map<String,File> outputFiles = new HashMap<>();
		FilenameFilter filter = (File dir, String name) -> name.endsWith("png");
		Arrays.stream(shadedOutputsDir.listFiles(filter)).forEach(f -> outputFiles.put(f.getName(), f));
		for(File f : inputsDir.listFiles(filter)) {
			System.out.println(f.getName());
			BufferedImage input = ImageIO.read(f);
			BufferedImage p2pOutput = ImageIO.read(outputFiles.get(f.getName().replace(".png", "-outputs.png")));
			BufferedImage out = blankImageTransparent(input.getWidth(), input.getHeight());
			for(int y=0;y<input.getHeight();y++) {
				for(int x=0;x<input.getWidth();x++) {
					if(input.getRGB(x, y) == FAMILY_FEATURE_COLOR.getRGB()) {
						out.setRGB(x, y, p2pOutput.getRGB(x, y));
					}
				}
			}

			ImageIO.write(out, "png",
					new File(cleanOutputDir + File.separator + f.getName()));
		}
	}

	public static ColorFamily analyzeColors(Set<Color> colorsFound) {
		List<SimilarColors> similars = new ArrayList<>();
		Set<Color> lonelies = new HashSet<>();
		// Set<Color> workingSet = new HashSet<Color>(colorsFound);
		List<Color> colorList = new LinkedList<Color>(colorsFound);

		for (int i = 0; i < colorList.size(); i++) {
			Color c = colorList.get(0);
			List<SimilarColors> sc = findMostSimilars(c, colorList, 25, 10);
			if (sc.isEmpty()) {
				lonelies.add(c);
			} else {
				similars.addAll(sc);
			}
			colorList.remove(c);
			System.out.println("Did a similars of size " + sc.size() + " with " + colorList.size() + " left");
		}
		ColorFamily family = new ColorFamily();
		for (SimilarColors sc : similars) {
			if (!family.isInFamily(sc.getC1())) {
				Set<Color> group = makeColorGroup(sc.getC1(), similars);
				group.addAll(makeColorGroup(sc.getC2(), similars));
				family.addGroup(group);
			}

		}

		for (Set<Color> group : family.getColorGroups()) {
			System.out.println("FAMILY");
			for (Color c : group) {
				System.out.println(c.getRed() + " " + c.getGreen() + " " + c.getBlue());
			}
		}

		return family;
	}

	public static Set<Color> makeColorGroup(Color c, Collection<SimilarColors> similars) {
		Set<Color> family = new HashSet<>();
		family.add(c);
		addFamilyMembers(family, similars);
		return family;
	}

	public static void addFamilyMembers(Set<Color> group, Collection<SimilarColors> similars) {

		boolean added = false;
		List<Color> concurrency = new ArrayList<Color>(group);
		for (Color fc : concurrency) {
			for (SimilarColors sc : similars) {
				if (sc.containsColor(fc)) {
					added = group.add(sc.getC1());
					added = added || group.add(sc.getC2());
				}
			}
		}
		if (added) {
			// for(Color fc : family) {
			addFamilyMembers(group, similars);
			// }
		}
	}

	public static Optional<SimilarColors> findMostSimilar(Color lonelyColor, Set<Color> colors,
			Set<SimilarColors> foundMatches) {
		SimilarColors bestMatch = null;
		for (Color c : colors) {
			if (!c.equals(lonelyColor)) {
				SimilarColors sc = new SimilarColors(lonelyColor, c);
				if (!foundMatches.contains(sc) && sc.getDiff() > 0 && sc.getDiff() < 40
						&& (bestMatch == null || sc.getDiff() < bestMatch.getDiff())) {
					bestMatch = sc;
				}
			}
		}
		if (bestMatch.getDiff() < 25) {
			return Optional.of(bestMatch);
		}
		return Optional.empty();
	}

	public static List<SimilarColors> findMostSimilars(Color lonelyColor, List<Color> colors, int threshold,
			int removeThreshold) {
		List<SimilarColors> bestMatches = new ArrayList<>();
		for (int i = 0; i < colors.size(); i++) {
			Color c = colors.get(i);
			if (!c.equals(lonelyColor)) {
				SimilarColors sc = new SimilarColors(lonelyColor, c);
				if (sc.getDiff() < threshold) {
					bestMatches.add(sc);
				}
				if (sc.getDiff() < removeThreshold) {
					colors.remove(sc.getC1());
					colors.remove(sc.getC2());
					i -= 2;
					i = i < 0 ? 0 : i;
				}
			}

		}

		return bestMatches;
	}

	public static BufferedImage centerImageOnGreen(BufferedImage in, int size) {
		Color background = new Color(0, 255, 0);
		BufferedImage out = ImageUtil.blankImage(size, size, background);
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				out.setRGB(x, y, background.getRGB());
			}
		}
		int offsetX = (size - in.getWidth()) / 2;
		int offsetY = (size - in.getHeight()) / 2;

		for (int y = 0; y < in.getHeight(); y++) {
			for (int x = 0; x < in.getWidth(); x++) {
				if (!isTransparent(in.getRGB(x, y))) {
					try {
						out.setRGB(x + offsetX, y + offsetY, in.getRGB(x, y));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return out;
	}

	public static Set<Color> getImageColors(BufferedImage image) {
		Set<Color> colors = new HashSet<>();
		Color background = new Color(image.getRGB(0, 0));

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Color c = new Color(image.getRGB(x, y));
				if (!background.equals(c)) {
					colors.add(c);
				}
			}
		}
		return colors;
	}

	public static int getColorGroupIndex(ColorFamily colorFamily, Color c) {
		for (int i = 0; i < colorFamily.size(); i++) {
			if (colorFamily.get(i).contains(c)) {
				if (i > 8) {
					return i;
				}
				return i;
			}
		}
		return -1;
	}

	public static void paintColorFamilies(BufferedImage input, BufferedImage out, ColorFamily colorFamilies) {
		List<Color> groupColors = new ArrayList<>();
		Color gc = new Color(100, 50, 50);
		for (int i = 0; i < colorFamilies.size(); i++) {
			groupColors.add(gc);
			gc = incrementFamilyColor(gc);
		}
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				int familyIdx = -1;
				for (int i = 0; i < colorFamilies.size(); i++) {
					if (colorFamilies.get(i).contains(inputC)) {
						familyIdx = i;
						break;
					}
				}
				if (familyIdx > -1) {
					out.setRGB(x, y, groupColors.get(familyIdx).getRGB());
				}
			}
		}
	}

	public static Color incrementFamilyColor(Color c) {
		if (c.getRed() < 220) {
			return new Color(c.getRed() + 45, c.getGreen(), c.getBlue());
		}
		if (c.getGreen() < 220) {
			return new Color(c.getRed(), c.getGreen() + 45, c.getBlue());
		}
		return new Color(c.getRed() + 45, c.getGreen(), c.getBlue() + 45);
	}

	public static List<Color> initColorGroupColors() {
		List<Color> colorGroupColors = new ArrayList<>();
		colorGroupColors.add(new Color(194, 75, 75));
		colorGroupColors.add(new Color(75, 75, 194));
		colorGroupColors.add(new Color(160, 120, 75));
		colorGroupColors.add(new Color(178, 64, 182));
		colorGroupColors.add(new Color(162, 162, 58));
		colorGroupColors.add(new Color(100, 100, 100));
		colorGroupColors.add(new Color(94, 60, 142));
		colorGroupColors.add(new Color(68, 134, 124));
		return colorGroupColors;
	}

	// Uses the input image's size to base
	public static void paintSingleGroup(int familyColorGroupIdx, BufferedImage input, String inputName, File outputDir,
			ColorFamily family) throws IOException {
		BufferedImage out = new BufferedImage(input.getWidth() * 2, input.getHeight(), input.getType());
		// copyImage(paintInput(initColorGroupColors(), input, family), out,
		// input.getWidth(), 0);
		List<Color> baseColors = initColorGroupColors();

		Optional<BufferedImage> groupColorPainted = paintSingleGroupInput(baseColors.get(0),
				paintInput(initColorGroupColors(), input, family), baseColors.get(familyColorGroupIdx));

		if (groupColorPainted.isEmpty()) {
			return; // don't output an empty training image
		}

		copyImage(groupColorPainted.get(), out, input.getWidth(), 0, false);
		copyImage(paintSingeGroup(family.get(familyColorGroupIdx), input, family), out, 0, 0, false);

		ImageIO.write(out, "png",
				new File(outputDir + File.separator + inputName.replace(".png", "_cg" + familyColorGroupIdx + ".png")));
	}

	public static BufferedImage paintSingeGroup(Set<Color> groupColors, BufferedImage input, ColorFamily family) {
		Color baseColor = initColorGroupColors().get(0);
		BufferedImage out = blankImage(input.getWidth(), input.getHeight(), new Color(input.getRGB(0, 0)));
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				if (groupColors.contains(inputC)) {
					out.setRGB(x, y, family.offsetLuminance(baseColor, inputC).getRGB());
				}
			}
		}
		return out;
	}

	private static Color randomColor(SecureRandom rand) {

		int R = randomColorPart(rand);
		int G = randomColorPart(rand);
		int B = randomColorPart(rand);
		// System.out.println(R+","+G+","+B);
		try {
		return new Color(R, G, B);
		} catch(IllegalArgumentException e) {
			System.out.println("Colors: "+R+", "+G+", "+B);
			throw e;
		}
	}

	private static int randomColorPart(SecureRandom rand) {
		byte[] r = new byte[4];
		rand.nextBytes(r);
		int mult = (r[0] | 1) == 1 ? 1 : -1;
		int c = 200+ (mult*Math.abs(ByteBuffer.wrap(r).getInt()) % 40);
		c = c > 255 ? 255 : c;
		return c;
	}

	public static Color randomColor(List<Color> baseColors) {
		SecureRandom rand = new SecureRandom();
		ColorDifference cd = null;
		Color c = null;
		while (cd == null || cd.getValue("DeltaE") < 20) {
			double minDiff = 9999999;
			for (Color bc : baseColors) {
				c = randomColor(rand);
				DifferenceAlgorithm differenceAlgo = fact.getAlgorithm("CIELab DE");
				CIELab tlab = colorToLab(bc);
				CIELab blab = colorToLab(c);
				cd = differenceAlgo.compute(tlab, blab);
				double d = cd.getValue("DeltaE");
				minDiff = minDiff > d ? d : minDiff;
			}

		}
		return c;
	}
	
	public static CIELab hexToLab(String hex) {
		return colorToLab(Color.decode(hex));
	}
	
	public static CIELab colorToLab(Color c) {
		XYZ WHITE = new XYZ(94.811, 100.0, 107.304);
		return new RGB(c.getRed(), c.getGreen(), c.getBlue()).toXYZ().toCIELab(WHITE);
	}
	
	public static Color parseHex(String hex) {
		if(hex.startsWith("#")) {
			hex = hex.substring(1);
		}
		return new Color(Integer.parseInt(hex, 16));
	}
	
	public static String toHex(int color) {
		return "#"+Integer.toHexString(color).substring(2);
	}
	public static String toHex(Color color) {
		return toHex(color.getRGB());
	}
	
	public static void backgroundToRandom(BufferedImage img, SecureRandom rand) {
		int background = GREEN_BG.getRGB();
		for(int y=0;y<img.getHeight();y++) {
			for(int x=0;x<img.getWidth();x++) {
				if(img.getRGB(x, y) == background) {
					img.setRGB(x, y, randomColor(rand).getRGB());
				}
			}
		}
	}
	
	public static BufferedImage paintToFamily(List<Color> baseColors, BufferedImage input, ColorFamily family,
			String fileName) {
		BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
		ImageColorFamily imageFamilyStats = new ImageColorFamily(family, input);
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				if (isBackgroundColor(inputC.getRGB())) {
					out.setRGB(x, y, inputC.getRGB());
				} else {
					try {
						// Color baseColor =
						// imageFamilyStats.getFamilyCounts().get(family.getColorGroup(inputC));
						int rank = imageFamilyStats.getGroupNumRank(family.getColorGroup(inputC));
						out.setRGB(x, y, family.offsetLuminance(baseColors.get(rank), inputC).getRGB());
					} catch (Exception e) {
						System.err.println("Need color family set for " + fileName + " " + x + "," + y);
					}
				}
			}
		}
		return out;
	}
	
	
	public static Optional<BufferedImage> paintToFamily(List<Color> baseColors, BufferedImage input, ImageColorFamily family,
			String fileName, int featuredColorIdx) {
		
		BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		boolean paintedFeatured = false;
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				int rank = family.getGroupNumRank(family.getColorGroup(inputC));
				if (isBackgroundColor(inputC.getRGB())) {
					// out.setRGB(x,y,randomColor(baseColors).getRGB());
					out.setRGB(x, y, inputC.getRGB());
				} else if(rank == featuredColorIdx) {
					try {
						out.setRGB(x, y, family.offsetLuminance(FAMILY_FEATURE_COLOR, inputC).getRGB());
						paintedFeatured = true;
					} catch (Exception e) {
						System.err.println("Need color family set for " + fileName + " " + x + "," + y);
					}
				} else {
					out.setRGB(x, y, GREEN_BG.getRGB());
				}
			}
		}
		if(paintedFeatured) {
			return Optional.of(out);
		}
		return Optional.empty();
	}
	

	// Returns Optional.empty if the input image didn't contain the family group
	// specified by groupColor
	public static Optional<BufferedImage> paintSingleGroupInput(Color outputColor, BufferedImage input,
			Color groupColor) {
		BufferedImage out = blankImage(input.getWidth(), input.getHeight(), GREEN_BG);
		boolean containsColor = false;
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				if (groupColor.equals(inputC)) {
					containsColor = true;
					out.setRGB(x, y, outputColor.getRGB());// baseColors.get(family.getColorGroup(inputC)).getRGB());
				}
			}
		}
		return containsColor ? Optional.ofNullable(out) : Optional.empty();
	}

	public static BufferedImage paintInput(List<Color> baseColors, BufferedImage input, ColorFamily family) {
		BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				if (isBackgroundColor(inputC.getRGB())) {
					// out.setRGB(x,y,randomColor(baseColors).getRGB());
					out.setRGB(x, y, inputC.getRGB());
				} else {
					out.setRGB(x, y, baseColors.get(family.getColorGroup(inputC)).getRGB());
				}
			}
		}
		return out;
	}
	
	public static BufferedImage paintInput(List<Color> baseColors, BufferedImage input, ImageColorFamily family, int featuredFamilyColorIdx) {
		int featured = new Color(220,220,0).getRGB();
		BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				//int rank = family.getColorGroup(inputC);
				int rank = family.getGroupNumRank(family.getColorGroup(inputC));
				if (isBackgroundColor(inputC.getRGB())) {
					// out.setRGB(x,y,randomColor(baseColors).getRGB());
					out.setRGB(x, y, inputC.getRGB());
				} else if(rank != featuredFamilyColorIdx) {
					out.setRGB(x, y, baseColors.get(family.getColorGroup(inputC)).getRGB());
				} else {
					out.setRGB(x, y,featured);
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
			if (inputFile.isDirectory()) {
				File outDir = new File(inputFile.getAbsoluteFile() + "_outlined");
				outDir.mkdir();
				File[] files = inputFile.listFiles((dirf, name) -> name.endsWith(".png"));
				for (File file : files) {
					outlineFile(file, outDir.getAbsolutePath());
				}
			} else {
				outlineSingleFile(inputFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void outlineSingleFile(File inputFile) throws IOException {
		String outFileName = inputFile.getAbsolutePath().replace(".png", "_outlined.png");
		outlineFile(inputFile, outFileName);
	}

	public static void outlineFile(File inputFile, String outFilePath) throws IOException {

		BufferedImage in = ImageIO.read(inputFile);
		BufferedImage out = ImageUtil.blankImage(in.getWidth(), in.getHeight(), Color.WHITE);
		for (int y = 1; y < out.getHeight() - 1; y++) {
			for (int x = 1; x < out.getWidth() - 1; x++) {
				if ((in.getRGB(x, y) & 0xff000000) != 0 && !isBackgroundColor(in.getRGB(x, y))) {
					boolean transparencyFound = false;
					for (int yb = y - 1; yb < y + 2; yb++) {
						for (int xb = x - 1; xb < x + 2; xb++) {
							transparencyFound = isBackgroundColor(in.getRGB(xb, yb)) || transparencyFound
									|| (in.getRGB(xb, yb) & 0xff000000) == 0;
						}
					}
					if (transparencyFound) {
						out.setRGB(x, y, Color.BLACK.getRGB() | 0xff000000);
					} else {
					}
				} else {
				}
			}
		}

		applyDoubleOutline(out, inputFile.getName(), outFilePath);
	}

	public static boolean isBackgroundColorImage(BufferedImage img) {
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if (!isBackgroundColor(img.getRGB(x, y))) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean exceedsMaxAcceptableBackgroundColorPercent(BufferedImage img, double Pct0To100) {
		double isBackground = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if (isBackgroundColor(img.getRGB(x, y))) {
					isBackground++;
				}
			}
		}
		double pct = (isBackground / (img.getWidth() * img.getHeight())) * 100;
		return pct >= Pct0To100;
	}

	public static boolean isBackgroundColor(int rgb) {
		Color c = new Color(rgb);
		return (c.getRed() == 0 && c.getBlue() == 0 && c.getGreen() == 255);
	}

	public static void applyDoubleOutline(BufferedImage in, String inName, String outPath) throws IOException {
		findBackgroundColor(in);
		Color background = Color.WHITE;// new Color(0, 255, 0);
		BufferedImage out = ImageUtil.blankImage(in.getWidth(), in.getHeight(), Color.WHITE);
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
						out.setRGB(x, y, Color.BLACK.getRGB());
					} else {
						out.setRGB(x, y, Color.WHITE.getRGB());
					}
				} else if (in.getRGB(x, y) == Color.BLACK.getRGB()) {
					out.setRGB(x, y, Color.BLACK.getRGB());
				} else {

				}
			}
		}
		ImageIO.write(out, "png", new File(outPath + File.separator + inName.replace("_outlined", "_doubleoutlined")));
	}

	public static int findBackgroundColor(BufferedImage img) {
		if (img.getRGB(0, 0) != img.getRGB(0, img.getHeight() - 1)
				|| img.getRGB(0, 0) != img.getRGB(img.getWidth() - 1, 0)
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