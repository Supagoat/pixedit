package q.pix.colorfamily;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetImage {
	private BufferedImage targetImage;
	private Map<Color, List<Point>> colorCoords;
	
	
	public TargetImage(BufferedImage targetImage) {
		setTargetImage(targetImage);
		setColorMap();
	}
	
	private void setColorMap() {
		if(getTargetImage() == null) {
			return;
		}
		colorCoords = new HashMap<>();
		for(int y=0;y<getTargetImage().getHeight();y++) {
			for(int x=0;x<getTargetImage().getWidth();x++) {
				Color c = new Color(getTargetImage().getRGB(x, y));
				if(!colorCoords.containsKey(c)) {
					colorCoords.put(c,new ArrayList<>());
				}
				colorCoords.get(c).add(new Point(x,y));
			}
		}
	}
	
	public BufferedImage getTargetImage() {
		return targetImage;
	}
	private void setTargetImage(BufferedImage targetImage) {
		this.targetImage = targetImage;
	}
	public Map<Color, List<Point>> getColorCoords() {
		return colorCoords;
	}
	public void setColorCoords(Map<Color, List<Point>> colorCoords) {
		this.colorCoords = colorCoords;
	}
	
}