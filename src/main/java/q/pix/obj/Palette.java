package q.pix.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The idea here is that we want to track the total palette of colors separately from the mapping
 * of the yellow color to the palette color, so the palette colors may want to be somewhat shared across
 * all the parts of the combined image.  I'm not 100% sure on this since they did each come from a different
 * color family so there actually shouldn't be any mapped color sharing, but we'll see how I feel about that.
 * 
 * It does provide a bit of a separation though of the concept of "these are the colors I want in this image" vs
 * "this is the color I want here" so I'm going to keep this separation of the paletteOfColorsRGB from the map for now
 */
public class Palette {
	private List<Integer> paletteOfColorsRGB;
	private Map<String,Map<Integer,Integer>> colorRGBToPaletteIdx; // Maps the RGB to an index in the palette, wrapped by a map of the file name to the palette map 
	
	public Palette() {
		paletteOfColorsRGB = new ArrayList<>();
		colorRGBToPaletteIdx = new TreeMap<>();
	}
	
	public Optional<Integer> getColorAtPaletteIdx(String fileName, int colorRGB) {

		Integer paletteColorIdx = getPaletteMap(fileName).get(colorRGB);
		if(paletteColorIdx == null) {
			return Optional.empty();
		}
		return Optional.of(getPaletteOfColorsRGB().get(paletteColorIdx));
	}
	
	public void setPaletteColor(String fileName, int yellowColor, int mappedToColor) {
		if(!getPaletteOfColorsRGB().contains(mappedToColor)) {
			getPaletteOfColorsRGB().add(mappedToColor);
		}
		getPaletteMap(fileName).put(yellowColor, getPaletteOfColorsRGB().indexOf(mappedToColor));
	}
	
	

	public List<Integer> getPaletteOfColorsRGB() {
		return paletteOfColorsRGB;
	}

	public void setPaletteOfColorsRGB(List<Integer> paletteOfColorsRGB) {
		this.paletteOfColorsRGB = paletteOfColorsRGB;
	}

	private Map<Integer, Integer> getPaletteMap(String fileName) {
		if(!colorRGBToPaletteIdx.containsKey(fileName)) {
			colorRGBToPaletteIdx.put(fileName, new TreeMap<>());
		}
		return colorRGBToPaletteIdx.get(fileName);
	}

	public void setColorRGBToPaletteIdx(Map<String,Map<Integer, Integer>> colorRGBToPaletteIdx) {
		this.colorRGBToPaletteIdx = colorRGBToPaletteIdx;
	}

	public Map<String, Map<Integer, Integer>> getColorRGBToPaletteIdx() {
		return colorRGBToPaletteIdx;
	}
	
	
	
}
