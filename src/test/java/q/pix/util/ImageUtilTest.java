package q.pix.util;

//import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;

//import org.junit.Test;

import q.pix.ui.pane.WorkspaceWindow;


public class ImageUtilTest {
	
//	@Test
//	public void calcOffsetTest() {
//		assertEquals(2, ImageUtil.calcOffset(WorkspaceWindow.IMAGE_SIZE-4));
//		assertEquals(1, ImageUtil.calcOffset(WorkspaceWindow.IMAGE_SIZE-3));
//		assertEquals(1, ImageUtil.calcOffset(WorkspaceWindow.IMAGE_SIZE-2));
//		assertEquals(0, ImageUtil.calcOffset(WorkspaceWindow.IMAGE_SIZE));
//		assertEquals(-2, ImageUtil.calcOffset(WorkspaceWindow.IMAGE_SIZE+4));
//	}
//	
//	@Test
//	public void smallImageDimensionsRemainTest() {
//		BufferedImage image = new BufferedImage(64, 128, BufferedImage.TYPE_INT_ARGB);
//		int black = Color.BLACK.getRGB();
//		for(int x=0;x<image.getWidth();x++) {
//			for(int y=0;y<image.getHeight();y++) {
//				image.setRGB(x, y, black);
//			}
//		}
//		
//		assertEquals(96, ImageUtil.calcOffset(image.getWidth()));
//		assertEquals(64, ImageUtil.calcOffset(image.getHeight()));
//		
//		BufferedImage scaled = ImageUtil.downscale(image);
//		assertEquals(WorkspaceWindow.IMAGE_SIZE, scaled.getWidth());
//		assertEquals(WorkspaceWindow.IMAGE_SIZE, scaled.getHeight());
//
//		for(int x=0;x<WorkspaceWindow.IMAGE_SIZE;x++) {
//			for(int y=0;y<WorkspaceWindow.IMAGE_SIZE;y++) {
//				if(x < 96 || x >= 160 || y < 64 || y >= 192) {
//					assertEquals("Red at "+x+","+y+" found "+ImageUtil.getRed(scaled.getRGB(x, y)), ImageUtil.getRed(Color.WHITE.getRGB()), ImageUtil.getRed(scaled.getRGB(x, y)));
//					assertEquals("Green at "+x+","+y+" found "+ImageUtil.getGreen(scaled.getRGB(x, y)), ImageUtil.getGreen(Color.WHITE.getRGB()), ImageUtil.getGreen(scaled.getRGB(x, y)));
//					assertEquals("Blue at "+x+","+y+" found "+ImageUtil.getBlue(scaled.getRGB(x, y)), ImageUtil.getBlue(Color.WHITE.getRGB()), ImageUtil.getBlue(scaled.getRGB(x, y)));
//				} else {
//					assertEquals("Red at "+x+","+y+" found "+ImageUtil.getRed(scaled.getRGB(x, y)), ImageUtil.getRed(Color.BLACK.getRGB()), ImageUtil.getRed(scaled.getRGB(x, y)));
//					assertEquals("Green at "+x+","+y+" found "+ImageUtil.getGreen(scaled.getRGB(x, y)), ImageUtil.getGreen(Color.BLACK.getRGB()), ImageUtil.getGreen(scaled.getRGB(x, y)));
//					assertEquals("Blue at "+x+","+y+" found "+ImageUtil.getBlue(scaled.getRGB(x, y)), ImageUtil.getBlue(Color.BLACK.getRGB()), ImageUtil.getBlue(scaled.getRGB(x, y)));
//				}
//			}
//		}
//	}
}