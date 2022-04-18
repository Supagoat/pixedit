package q.pix.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import q.pix.colorfamily.ColorFamily;
import q.pix.colorfamily.FamilyAffinity;

public class FileUtil {

	public static final String INPUT_DIR = "input";
	public static final String TARGET_DIR = "target";
	public static final String TRAIN_DIR = "trainset";
	public static final String TEST_DIR = "testset";

	private static final String FAMILY_DIVIDER = "-----";

	public static String toInputDir(String dir) {
		return dir.replace(File.separator + TARGET_DIR + File.separator, File.separator + INPUT_DIR + File.separator);
	}

	public static String toTargetDir(String dir) {
		return dir.replace(File.separator + INPUT_DIR + File.separator, File.separator + TARGET_DIR + File.separator);
	}

	public static String inputToTestsetOutputDir(String dir) {
		int lastDirIdx = dir.lastIndexOf(File.separator);
		return dir.substring(0, lastDirIdx) + File.separator + TEST_DIR;
	}

	public static Optional<FamilyAffinity> loadConfigFiles(Set<Color> imageColors, File dir) {

		List<FamilyAffinity> affinities = new ArrayList<>();

		for (File config : dir.listFiles()) {
			Optional<ColorFamily> family = loadConfig(config);
			if (family.isPresent()) {
				affinities.add(new FamilyAffinity(imageColors, family.get()));
			}
		}

		if (affinities.size() > 0)

		{
			Collections.sort(affinities);
			if (affinities.get(0).isMatchingAffinity(imageColors)) {
				return Optional.of(affinities.get(0));
			}
		}

		return Optional.empty();
	}

	public static Optional<ColorFamily> loadConfig(File file) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			ColorFamily configFamily = new ColorFamily(file.getName());
			if (line.equals(FAMILY_DIVIDER)) { // it's a config file!
				Set<Color> currentFamily = null;
				while (line != null) {
					if (FAMILY_DIVIDER.contentEquals(line)) {
						if (currentFamily != null) {
							configFamily.addGroup(currentFamily);
						}
						currentFamily = new HashSet<>();
					} else {
						String[] colorStr = line.split(",");
						currentFamily.add(new Color(Integer.parseInt(colorStr[0]), Integer.parseInt(colorStr[1]),
								Integer.parseInt(colorStr[2])));
					}
					line = in.readLine();
					
				}
				in.close();
				if (currentFamily != null && !configFamily.getColorGroups().contains(currentFamily)) {
					configFamily.addGroup(currentFamily);
				}
				for(int i=configFamily.size();i<8;i++) {
					configFamily.addGroup(new HashSet<>());
				}
				
				return Optional.of(configFamily);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static void writeFamilyConfigFile(ColorFamily colorFamily, String outputFilepath, String inputFileName) {

		try {
			PrintWriter out = new PrintWriter(
					new FileWriter(new File(outputFilepath + File.separator + inputFileName.replace(".png", ".txt"))));
			for (Set<Color> family : colorFamily.getColorGroups()) {
				out.println(FAMILY_DIVIDER);
				for (Color c : family) {
					out.println(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
				}
			}
			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static File getFamilyConfigDir(File from) {
		String basePath = from.getAbsolutePath();
		if (from.getName().endsWith(".png")) {
			basePath = basePath.substring(0, from.getAbsolutePath().lastIndexOf(File.separator));
		}
		basePath = basePath.substring(0, basePath.length() - 1);
		basePath = basePath.substring(0, basePath.lastIndexOf(File.separator));
		File configDir = new File(basePath + File.separator + "colorFamilies");
		if (!configDir.exists()) {
			configDir.mkdir();
		}

		return configDir;
	}

}