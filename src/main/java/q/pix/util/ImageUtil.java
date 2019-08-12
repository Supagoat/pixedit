package q.pix.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

	public static void outlineFile(File inputFile) throws IOException {

		BufferedImage in = ImageIO.read(inputFile);
		BufferedImage out = ImageUtil.blankImage(in.getWidth(), in.getHeight(),
				new Color(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 0));
		Graphics2D g = (Graphics2D) out.createGraphics();
		g.drawLine(100, 100, 500, 500);
		AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
		g.setComposite(composite);
		for (int y = 1; y < out.getHeight() - 1; y++) {
			for (int x = 1; x < out.getWidth() - 1; x++) {
				// System.out.println((in.getRGB(x, y) & 0xff000000));
				if ((in.getRGB(x, y) & 0xff000000) != 0) {
					boolean transparencyFound = false;
					for (int yb = y - 1; yb < y + 2; yb++) {
						for (int xb = x - 1; xb < x + 2; xb++) {
							transparencyFound = transparencyFound || (in.getRGB(xb, yb) & 0xff000000) == 0;
						}
					}
					if (transparencyFound) {
						out.setRGB(x, y, Color.BLACK.getRGB() | 0xff000000);
					} else {
						out.setRGB(x, y, Color.WHITE.getRGB());
					}
				} else {
					// out.setRGB(x, y, Color.WHITE.getRGB());
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(x, y, 1, 1);
				}
			}
		}
		ImageIO.write(out, "png", new File(inputFile.getAbsolutePath().replace(".png", "_outlined.png")));
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