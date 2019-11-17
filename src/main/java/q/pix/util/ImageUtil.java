package q.pix.util;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import q.pix.colorfamily.ColorFamily;
import q.pix.colorfamily.FamilyAffinity;
import q.pix.colorfamily.SimilarColors;

public class ImageUtil {
	public static final int IMAGE_WIDTH = 256;
	public static final int IMAGE_HEIGHT = 256;
	public static final int CROPPABLE_IMAGE_WIDTH = 286;
	public static final int CROPPABLE_IMAGE_HEIGHT = 286;
	public static final Color GREEN_BG = new Color(0, 255, 0);

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
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

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
			BufferedImage inputImage;

			if (blanksWhereMissing) {
				inputImage = ImageIO.read(input);
			} else {
				inputImage = copyIntoCenter(ImageIO.read(input),
						blankImage(CROPPABLE_IMAGE_WIDTH, CROPPABLE_IMAGE_HEIGHT, GREEN_BG));
			}

			BufferedImage targetImage = null;
//			if (target != null && target.exists()) {
//				targetImage = ImageIO.read(target);
//			} else {
//				targetImage = blankImage(inputImage.getWidth(), inputImage.getHeight(), GREEN_BG);
//			}

			if (blanksWhereMissing) {
				if (target != null && target.exists()) {
					targetImage = ImageIO.read(target);
				} else {
					targetImage = blankImage(inputImage.getWidth(), inputImage.getHeight(), GREEN_BG);
				}
			} else {
				targetImage = copyIntoCenter(ImageIO.read(target),
						blankImage(CROPPABLE_IMAGE_WIDTH, CROPPABLE_IMAGE_HEIGHT, GREEN_BG));
			}

			int width = blanksWhereMissing ? IMAGE_WIDTH : inputImage.getWidth();
			int height = blanksWhereMissing ? IMAGE_HEIGHT : inputImage.getHeight();
			combineImage(targetImage, inputImage, outputDir, input.getName(), width, height);
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
				ImageIO.write(output, "png", new File(outputDir + File.separator + namePrefix + outputNameBase));
			}
		}

	}

	public static BufferedImage copyImage(BufferedImage input) {
		return copyImage(input, 0, 0);
	}

	public static BufferedImage copyImage(BufferedImage input, int xOffset, int yOffset) {
		BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
		return copyImage(input, output, xOffset, yOffset);
	}

	public static BufferedImage copyImage(BufferedImage input, BufferedImage output, int xOffset, int yOffset) {
		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				output.setRGB(x + xOffset, y + yOffset, input.getRGB(x, y));
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

	public static void paintFamilyTrainings(List<Color> baseColors, File inputFile, ColorFamily family, File outDir) {
		for (Set<Color> groupColors : family.getColorGroups()) {
			if (!groupColors.isEmpty()) {

			}
		}
	}

	public static void paintToFamily(List<Color> baseColors, File inputFile, ColorFamily family, File outDir)
			throws IOException {
		BufferedImage painted = paintToFamily(baseColors, ImageIO.read(inputFile), family);
		ImageIO.write(painted, "png", new File(outDir.getAbsoluteFile() + File.separator + inputFile.getName()));
	}

	public static void paintInput(List<Color> baseColors, File inputFile, ColorFamily family, File outDir)
			throws IOException {
		BufferedImage painted = paintInput(baseColors, ImageIO.read(inputFile), family);
		ImageIO.write(painted, "png", new File(outDir.getAbsoluteFile() + File.separator + inputFile.getName()));
	}

	// Not using analyze colors right now because LAB still ends up grouping some
	// colors together in ways I don't like
	public static void analyzeColors(File inputFile) throws IOException {
		BufferedImage analyzed = analyzeColors(ImageIO.read(inputFile));
		ImageIO.write(analyzed, "png", new File(inputFile.getAbsolutePath().replace(".png", "_colors.png")));
	}

	public static Set<Color> getDistinctColors(BufferedImage img) {
		Set<Color> colorsFound = new HashSet<>();
		Color inputBackground = new Color(img.getRGB(0, 0));
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

			SimilarColors sc = findMostSimilar(c, colorsFound, similars);
			if (sc == null) {
				lonelies.add(c);
			} else {
				similars.add(sc);
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

	public static Set<Color> makeColorGroup(Color c, Set<SimilarColors> similars) {
		Set<Color> family = new HashSet<>();
		family.add(c);
		addFamilyMembers(family, similars);
		return family;
	}

	public static void addFamilyMembers(Set<Color> group, Set<SimilarColors> similars) {

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

	public static SimilarColors findMostSimilar(Color lonelyColor, Set<Color> colors, Set<SimilarColors> foundMatches) {
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
			return bestMatch;
		}
		return null;
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
		if (c.getRed() < 250) {
			return new Color(c.getRed() + 50, c.getGreen(), c.getBlue());
		}
		if (c.getGreen() < 250) {
			return new Color(c.getRed(), c.getGreen() + 50, c.getBlue());
		}
		return new Color(c.getRed() + 50, c.getGreen(), c.getBlue() + 50);
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
	public static void paintSingeGroup(int familyColorGroupIdx, BufferedImage input, String inputName, File outputDir,
			ColorFamily family) throws IOException {
		BufferedImage out = new BufferedImage(input.getWidth() * 2, input.getHeight(), input.getType());
		//copyImage(paintInput(initColorGroupColors(), input, family), out, input.getWidth(), 0);
		List<Color> baseColors = initColorGroupColors();
		
		Optional<BufferedImage> groupColorPainted = paintSingleGroupInput(baseColors.get(0), paintInput(initColorGroupColors(), input, family), baseColors.get(familyColorGroupIdx));
		
		if(groupColorPainted.isEmpty()) {
			return; // don't output an empty training image
		}
		
		copyImage(groupColorPainted.get(), out, input.getWidth(), 0);
		copyImage(paintSingeGroup(family.get(familyColorGroupIdx), input, family), out, 0, 0);
		
		ImageIO.write(out, "png", new File(outputDir + File.separator + inputName.replace(".png", "_cg"+familyColorGroupIdx+".png")));
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

	public static BufferedImage paintToFamily(List<Color> baseColors, BufferedImage input, ColorFamily family) {
		BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				if (isBackgroundColor(inputC.getRGB())) {
					out.setRGB(x, y, inputC.getRGB());
				} else {
					out.setRGB(x, y,
							family.offsetLuminance(baseColors.get(family.getColorGroup(inputC)), inputC).getRGB());
				}
			}
		}
		return out;
	}

	// Returns Optional.empty if the input image didn't contain the family group specified by groupColor
	public static Optional<BufferedImage> paintSingleGroupInput(Color outputColor, BufferedImage input, Color groupColor) {
		BufferedImage out = blankImage(input.getWidth(), input.getHeight(),GREEN_BG);
		boolean containsColor = false;
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color inputC = new Color(input.getRGB(x, y));
				if (groupColor.equals(inputC)) {
					containsColor = true;
					out.setRGB(x, y, outputColor.getRGB());//baseColors.get(family.getColorGroup(inputC)).getRGB());
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
					out.setRGB(x, y, inputC.getRGB());
				} else {
					out.setRGB(x, y, baseColors.get(family.getColorGroup(inputC)).getRGB());
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