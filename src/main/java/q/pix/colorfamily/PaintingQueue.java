package q.pix.colorfamily;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;

import q.pix.util.FileUtil;
import q.pix.util.ImageUtil;

public class PaintingQueue {
	// private Map<String, ColorFamily> needsFamilyPainting;
	private List<ColorFamily> colorFamilies;
	private ColorFamily currentFamily;
	private String inputFileName;
	private Iterator<File> files;
	private BiConsumer<BufferedImage, ColorFamily> imageConsumer;
	private File configDir;
	
	
	public PaintingQueue() {
		// needsFamilyPainting = new HashMap<>();
	}

	public void setup(File inputDir, File savedFamiliesDir, BiConsumer<BufferedImage, ColorFamily> imageDestination) {
		try {
			setConfigDir(savedFamiliesDir);
			setColorFamilies(loadConfigs());

			queueImages(inputDir, imageDestination);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<ColorFamily> loadConfigs() {
		List<ColorFamily> families = new ArrayList<>();
		for (File configFile : getConfigDir().listFiles()) {
			Optional<ColorFamily> config = FileUtil.loadConfig(configFile);
			if (config.isPresent()) {
				families.add(config.get());
			}
		}
		return families;
	}

	private void queueImages(File inputDir, BiConsumer<BufferedImage, ColorFamily> imageConsumer) {
		setFiles(Arrays.asList(inputDir.listFiles((dirf, name) -> name.endsWith(".png"))).iterator());
		setImageConsumer(imageConsumer);
		queueStep(true);
	}

	public void queueStep(boolean placeholder) {
		while (getFiles().hasNext()) {
			try {
				setColorFamilies(loadConfigs());
				File imgFile = getFiles().next();
				setInputFileName(imgFile.getName());
				BufferedImage img = ImageIO.read(imgFile);
				Set<Color> imgColors = ImageUtil.getDistinctColors(img);
				FamilyAffinity closestFamily = ImageUtil.findClosestColorFamily(imgColors, getColorFamilies());
				if (closestFamily != null && closestFamily.getColorFamily().containsAllColors(imgColors)) {
					System.out.println("Skipping since I already have a family for "+imgFile.getName());
					continue;
				}
				setCurrentFamily(new ColorFamily());
				getCurrentFamily().setFamilyName(imgFile.getName());
				if (closestFamily != null && closestFamily.isMatchingAffinity(imgColors)) {
					setCurrentFamily(closestFamily.getColorFamily());
				}
				
				if(closestFamily != null) {
					System.out.println(imgFile.getName()+" with color family "+getCurrentFamily().getFamilyName()+" match rate of "+closestFamily.calcFamilyColorPct(imgColors));
					System.out.println("Not in family: "+closestFamily.getMissingColors(imgColors));
				}
				BufferedImage inputImage = ImageUtil.loadAndScale(imgFile);
				getImageConsumer().accept(inputImage, getCurrentFamily());
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("All done!");
	}

	public BiConsumer<BufferedImage, ColorFamily> getImageConsumer() {
		return imageConsumer;
	}

	public void setImageConsumer(BiConsumer<BufferedImage, ColorFamily> imageConsumer) {
		this.imageConsumer = imageConsumer;
	}

	public Iterator<File> getFiles() {
		return files;
	}

	public void setFiles(Iterator<File> files) {
		this.files = files;
	}

	public ColorFamily getCurrentFamily() {
		return currentFamily;
	}

	public void setCurrentFamily(ColorFamily currentFamily) {
		this.currentFamily = currentFamily;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	private List<ColorFamily> getColorFamilies() {
		return colorFamilies;
	}

	private void setColorFamilies(List<ColorFamily> colorFamilies) {
		this.colorFamilies = colorFamilies;
	}

	public File getConfigDir() {
		return configDir;
	}

	public void setConfigDir(File configDir) {
		this.configDir = configDir;
	}



}
