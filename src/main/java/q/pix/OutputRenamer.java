package q.pix;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class OutputRenamer {

	public static void main(String[] args) throws Exception {
		List<File> files = Arrays.asList(new File("D:\\art\\p2p\\roto1make\\roto1make\\forAnimation").listFiles());
		int counter = 1;
		for (File f : files) {
//			if(f.getName().contains("input")) {
//				f.delete();
//				continue;
//			}
			
			//String newName = f.getAbsolutePath().split("-")[0]+".png";
			
			//String newName = f.getAbsolutePath().substring(0, f.getAbsolutePath().indexOf(".png"))+".png";
			
			String newName = f.getAbsolutePath().substring(0, f.getAbsolutePath().indexOf("2707"));
			newName = newName+String.format("%05d", counter);
			counter++;
			newName = newName+".png";
			System.out.println(newName);
			f.renameTo(new File(newName));
		}
	}
}