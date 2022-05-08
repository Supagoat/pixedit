package external;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Fishtest {

	public static void main(String[] args) throws Exception {
		BufferedImage i = new BufferedImage(7952, 5304, BufferedImage.TYPE_INT_RGB);
		int[] center = {7952/2, 5304/2};
		int[] colors = {0,0,0};
		int mod = 0;
		
		for(int y=0;y<i.getHeight();y++) {
			for(int x=0;x<i.getWidth();x++) {
				Color c = new Color(colors[0], colors[1], colors[2]);
				i.setRGB(x, y, c.getRGB());
				colors[mod]++;
				mod++;
				mod = mod == 3 ? 0 : mod;
				if(mod == 0 && colors[2] == 255) {
					mod = 0;
					colors = new int[] {0,0,0};
				}
			
			}
		}
		
		ImageIO.write(i, "png", new File("D:\\tmp\\fishtest.png"));
		
	}
	
}
