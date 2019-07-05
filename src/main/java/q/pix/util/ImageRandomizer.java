package q.pix.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageRandomizer {
	List<File> inFiles = new ArrayList<>();
	public static final int BLOCK_SIZE=32;
	public static void main(String[] args) throws Exception {
		ImageRandomizer randomizer = new ImageRandomizer();
		randomizer.load(new File(args[0]));
		randomizer.process(Integer.parseInt(args[1]), args[2]);
	}

	public void load(File f) {
		if(f.isDirectory()) {
			loadDir(f.listFiles(new FileFilter() { 
				public boolean accept(File f)  {
					return f.getName().contains("_in.png") && new File(inToTarget(f)).exists();}}));
		} else {
			inFiles.add(f);
		}
		
	}

	public void loadDir(File[] files) {
		for (File f : files) {
			load(f);
		}
	}
	
	public void process(int outputImageCount, String outputDir) throws Exception {
		BufferedImage in_out = ImageUtil.blankImage();
		BufferedImage target_out = ImageUtil.blankImage();
		for(int i=0;i<outputImageCount;i++) {
			for(int x=0;x<in_out.getWidth()/BLOCK_SIZE;x+=BLOCK_SIZE) {
				for(int y=0;y<in_out.getHeight()/BLOCK_SIZE;y+=BLOCK_SIZE) {
					int imgIdx = (int)(Math.random()*inFiles.size());
					BufferedImage in_in = ImageIO.read(inFiles.get(imgIdx));
					BufferedImage in_target = ImageIO.read(new File(inToTarget(inFiles.get(imgIdx))));
					
					int sourceX = (int)(Math.random()*(in_out.getWidth()-BLOCK_SIZE));
					int sourceY = (int)(Math.random()*(in_out.getHeight()-BLOCK_SIZE));
					in_out.getGraphics().drawImage(in_in, x, y, x+BLOCK_SIZE, y+BLOCK_SIZE, sourceX, sourceY, sourceX+BLOCK_SIZE, sourceY+BLOCK_SIZE,null);
					target_out.getGraphics().drawImage(in_target, x, y, x+BLOCK_SIZE, y+BLOCK_SIZE, sourceX, sourceY, sourceX+BLOCK_SIZE, sourceY+BLOCK_SIZE,null);
					//in_out.getGraphics().drawImage(in_in, x, y, BLOCK_SIZE, BLOCK_SIZE, null);
					//target_out.getGraphics().drawImage(in_target, x, y, BLOCK_SIZE, BLOCK_SIZE, null);
				}
			}
			ImageUtil.combineImage(target_out, in_out, outputDir, "gt_"+i+".png");
		}
	}
	
	
	private String inToTarget(File in) {
		return in.getAbsolutePath().replace("in", "target");
	}

}
