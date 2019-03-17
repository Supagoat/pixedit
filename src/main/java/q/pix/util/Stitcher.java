package q.pix.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Puts IMAGE_SIZE cut up output back together into single files
 */
public class Stitcher {
	public static void main(String[] args) throws Exception {
		String inputDir = args[0];
		String filter = args[1];
		
		File[] toCombine = new File(inputDir).listFiles((dir, name) -> filter.equals("*") ? true : name.contains(filter));
		
		Map<String,List<File>> fileMap = gatherFrames(toCombine);
		fileMap.entrySet().forEach(System.out::println);
	}
	
	public static Map<String,List<File>> gatherFrames(File[] files) {
		Map<String,List<File>> fileMap = new HashMap<>();
		for(File f : files) {
			String imgName = f.getName().substring(f.getName().indexOf("IMG_"), f.getName().indexOf("_", f.getName().indexOf("IMG_")+5));
			List<File> imgFiles = fileMap.get(imgName);
			if(imgFiles == null) {
				imgFiles = new ArrayList<>(100);
				fileMap.put(imgName, imgFiles);
			}
			imgFiles.add(f);
		}
		return fileMap;
	}

}