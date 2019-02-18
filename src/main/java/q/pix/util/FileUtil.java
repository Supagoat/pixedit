package q.pix.util;

import java.io.File;

public class FileUtil {
	
	public static final String INPUT_DIR = "input";
	public static final String TARGET_DIR = "target";
	public static final String TRAIN_DIR = "trainset";
	public static final String TEST_DIR = "testset";
	
	public static String toTargetDir(String dir) {
		return dir.replace(File.separator + TARGET_DIR + File.separator,
				File.separator + INPUT_DIR + File.separator);
	}
	
	public static String toInputDir(String dir) {
		return dir.replace(File.separator + INPUT_DIR + File.separator,
		File.separator + TARGET_DIR + File.separator);
	}
	
	public static String inputToTestsetOutputDir(String dir) {
		int lastDirIdx = dir.lastIndexOf(File.separator);
		return dir.substring(0, lastDirIdx)+File.separator+TEST_DIR;
	}
}