package q.pix.obj;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import q.pix.util.ImageUtil;

/**
 * The idea here is that we want to track the total palette of colors separately from the mapping
 * of the luminance color to the palette color, so the palette colors may want to be somewhat shared across
 * all the parts of the combined image as well as multiple images where the luminances don't match exactly.  
 * The idea is to map the position in the luminance sort to an output color
 * I'm not 100% sure on this since they did each come from a different
 * color family so there actually shouldn't be any mapped color sharing, but we'll see how I feel about that.
 * 
 * It does provide a bit of a separation though of the concept of "these are the colors I want in this image" vs
 * "this is the color I want here" so I'm going to keep this separation of the paletteOfColorsRGB from the map for now
 */
public class Palette {
	private List<String> paletteOfColorsHex;
	//private Map<String,Map<String,Integer>> colorRGBToPaletteIdx; // Maps the RGB of a luminance to an index in the palette, wrapped by a map of the file name to the palette map 
	private Map<String, ImageLuminanceIdxs> imgLuminanceIdxs; // A map of a filename to the sort order of its luminances
	private Map<String,Map<Integer,Integer>> orderedOutputColors; // Outer map is keyed on filename.  Inner map is the index of the luminance (sorted by luminance) to the index in paletteOfColorsHex
	public Palette() {
		paletteOfColorsHex = new ArrayList<>();
		orderedOutputColors = new HashMap<>();
		imgLuminanceIdxs = new HashMap<>();
	}
	
	public void mapLuminanceIdxs(Map<String, BufferedImage> fileImages) {
		for(Map.Entry<String, BufferedImage> file : fileImages.entrySet() ) {
			imgLuminanceIdxs.put(file.getKey(), new ImageLuminanceIdxs(file.getKey(), file.getValue()));
		}
	}
	
//	public Optional<Integer> getColorIndex(String fileName, int luminanceColor) {
//		
//	}
	
	
	public Optional<Color> getMappedColorForLuminanceIndex(String fileName, int luminanceIdx) {
		Map<Integer,Integer> mapped = getPaletteMap(fileName);
		if(mapped.containsKey(luminanceIdx)) {
			return Optional.of(ImageUtil.parseHex(getPaletteOfColorsHex().get(mapped.get(luminanceIdx))));
		}
		return Optional.empty();
	}
	
	public Optional<Color> getMappedColorForLuminance(String fileName, Color luminance) {
		int luminanceIdx = getImageLuminanceIdx(fileName, luminance);
		return getMappedColorForLuminanceIndex(fileName, luminanceIdx);
	}
	
	public Optional<Color> getColorAtPaletteIdx(String fileName, int paletteIdx) {

		Integer paletteColorIdx = getPaletteMap(fileName).get(paletteIdx);
		if(paletteColorIdx == null) {
			return Optional.empty();
		}
		return Optional.of(ImageUtil.parseHex(getPaletteOfColorsHex().get(paletteColorIdx)));
	}
	
	public void setPaletteColor(String fileName, Color luminanceColor, Color mappedToColor) {
		setPaletteColor(fileName, getImageLuminanceIdx(fileName,luminanceColor), ImageUtil.toHex(mappedToColor.getRGB()));
	}
	
	public void setPaletteColor(String fileName, int imageLuminanceIdx, String mappedToColor) {
		if(!getPaletteOfColorsHex().contains(mappedToColor)) {
			getPaletteOfColorsHex().add(mappedToColor);
		}
		getPaletteMap(fileName).put(imageLuminanceIdx, getPaletteOfColorsHex().indexOf(mappedToColor));
	}
	
	public int getImageLuminanceIdx(String fileName, Color luminance) {
		return imgLuminanceIdxs.get(fileName).getImageLuminanceIdx(ImageUtil.toHex(luminance));
	}
	
	public int getImageLuminanceIdx(String fileName, String luminanceHex) {
		return imgLuminanceIdxs.get(fileName).getImageLuminanceIdx(luminanceHex);
	}
	
	public List<String> getImageLuminancesSorted(String fileName) {
		return imgLuminanceIdxs.get(fileName).getImageHexIdxs();
	}

	public List<String> getPaletteOfColorsHex() {
		return paletteOfColorsHex;
	}

	public void setPaletteOfColorsHex(List<String> paletteOfColorsHex) {
		this.paletteOfColorsHex = paletteOfColorsHex;
	}

	private Map<Integer, Integer> getPaletteMap(String fileName) {
		if(!orderedOutputColors.containsKey(fileName)) {
			orderedOutputColors.put(fileName, new TreeMap<>());
		}
		return orderedOutputColors.get(fileName);
	}

	public void setColorRGBToPaletteIdx(Map<String,Map<Integer, Integer>> colorRGBToPaletteIdx) {
		this.orderedOutputColors = colorRGBToPaletteIdx;
	}

	public Map<String, Map<Integer, Integer>> getColorRGBToPaletteIdx() {
		return orderedOutputColors;
	}
	
	
	
}
