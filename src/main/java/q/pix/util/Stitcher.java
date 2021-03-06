package q.pix.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Puts IMAGE_SIZE cut up output back together into single files
 */
public class Stitcher {
	public static void main(String[] args) throws Exception {
		int width = Integer.parseInt(args[0]);
		int height = Integer.parseInt(args[1]);
		String inputDir = args[2];
		String outputDir = args[3];
		String filter = args.length == 4 ? "*" : args[4];
		
		new File(outputDir).mkdirs();
		
		File[] toCombine = new File(inputDir).listFiles((dir, name) -> filter.equals("*") ? true : name.contains(filter));
		
		Map<String,List<File>> fileMap = gatherFrames(toCombine);
		for(Map.Entry<String, List<File>> batch : fileMap.entrySet()) {
			combineImage(width, height, batch.getKey(), batch.getValue(), outputDir);
		}
	}
	
	public static void combineImage(int width, int height, String outputName, List<File> fSet, String outputDir) throws IOException {
		System.out.println("Processing "+outputName);
		BufferedImage img = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
		for(File f : fSet) {
			BufferedImage inputImage = ImageIO.read(f);
			int xOff = Integer.parseInt(f.getName().split("_")[1]);
			int yOff = Integer.parseInt(f.getName().split("_")[2]);
			//System.out.println(xOff+","+yOff+" gpomg to "+(inputImage.getWidth()+xOff)+","+(inputImage.getHeight()+yOff));
			int heightSlice = inputImage.getHeight()/4;
			int widthSlice = inputImage.getWidth()/4;
			int left = xOff == 0 ? 0 : widthSlice;
			int top =  yOff == 0 ? 0 : heightSlice;
			System.out.println(xOff+" "+left);
			int right = widthSlice*3;
			int bot = heightSlice*3;
//			for(int x=0;x<inputImage.getWidth();x++) {
//				for(int y=0;y<inputImage.getWidth();y++) {
//					if(x==y || inputImage.getWidth()-x == y || x == 0 || y == 0 || x == left || y == top || x == right || y == bot) {
//						inputImage.setRGB(x, y, Color.RED.getRGB());
//					}
//				}
//			}
			img.getGraphics().drawImage(inputImage, xOff+left, yOff+top, xOff+widthSlice*3, yOff+heightSlice*3, 
					left,top, right, bot, null);
		}
		if(!new File(outputDir+File.separator+outputName).exists()) {
			ImageIO.write(img, "png", new File(outputDir+File.separator+outputName));
		}
	}
	
	public static Map<String,List<File>> gatherFrames(File[] files) {
		Map<String,List<File>> fileMap = new HashMap<>();
		for(File f : files) {
			if(!f.getName().startsWith("_")) {
				continue;
			}
			String[] bits = f.getName().split("_");
			String[] nameBits = Arrays.copyOfRange(bits, 3, bits.length);
			String imgName = String.join("_", nameBits);

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