package q.pix;

import java.io.File;

public class DeWaifuname {

public static void main(String[] args) throws Exception {
			for(File f : new File(args[0]).listFiles()) {
				f.renameTo(new File(f.getAbsolutePath().substring(0,  f.getAbsolutePath().length()-1)));
			}

		}

}
