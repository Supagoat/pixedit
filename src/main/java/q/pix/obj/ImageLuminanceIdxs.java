package q.pix.obj;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import q.pix.util.ImageUtil;
import q.pix.util.LuminanceComparator;

public class ImageLuminanceIdxs {
	private List<String> imageHexIdxs; // Elements are the hex, ordering in list indicates index
	private String imgName; // Mostly here to double-check against other data
	
	public ImageLuminanceIdxs(String imgName, BufferedImage img) {
		this.imgName = imgName;
		imageHexIdxs = new ArrayList<>();
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int imgColor = img.getRGB(x, y);
				int alpha = (imgColor & 0xFF000000);
				if (alpha != 0) {
					String hex ="#"+Integer.toHexString(imgColor).substring(2);
					if(!imageHexIdxs.contains(hex)) {
						imageHexIdxs.add(hex);
					}
				}
			}
		}
		imageHexIdxs.sort(new LuminanceComparator());
	}
	public int getImageLuminanceIdx(Color color) {
		return getImageLuminanceIdx(ImageUtil.toHex(color));
	}
	public int getImageLuminanceIdx(String hex) {
		return getImageHexIdxs().indexOf(hex);
	}

	public List<String> getImageHexIdxs() {
		return imageHexIdxs;
	}

	public void setImageHexIdxs(List<String> imageHexIdxs) {
		this.imageHexIdxs = imageHexIdxs;
	}

	public String getImgName() {
		return imgName;
	}

	public void setImgName(String imgName) {
		this.imgName = imgName;
	}
	
	
}