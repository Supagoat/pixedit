package q.pix.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class ImageRandomizer {
	List<File> inFiles = new ArrayList<>();
	public static void main(String[] args) {

	}

	public void load(File f) {
		if(f.isDirectory()) {
			loadDir(f.listFiles(new FileFilter() { 
				public boolean accept(File f)  {
					return f.getName().contains("_in.png") && new File(f.getAbsolutePath().replace("in", "target")).exists();}}));
		} else {
			inFiles.add(f);
		}
		
	}

	public void loadDir(File[] files) {
		for (File f : files) {
			load(f);
		}
	}

}
