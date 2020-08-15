package q.pix;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class WaifuDriver {
	public static void main(String[] args) throws Exception {
		PrintWriter out = new PrintWriter(new FileWriter(new File(args[1])));
		for(File f : new File(args[0]).listFiles()) {
			out.println("th waifu2x.lua -m scale -i split_inputs/"+f.getName()+" -o split_resized/"+f.getName());
		}
		out.flush();
		out.close();
	}
}