package q.pix.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
	

	public List<Integer> getPaletteOfColorsRGB() {
		return paletteOfColorsRGB;
	}

	public void setPaletteOfColorsRGB(List<Integer> paletteOfColorsRGB) {
		this.paletteOfColorsRGB = paletteOfColorsRGB;
	}

	public Map<Integer, Integer> getPaletteMap(String fileName) {
		return colorRGBToPaletteIdx.get(fileName);
	}

	public void setColorRGBToPaletteIdx(Map<String,Map<Integer, Integer>> colorRGBToPaletteIdx) {
		this.colorRGBToPaletteIdx = colorRGBToPaletteIdx;
	}

	public Map<String, Map<Integer, Integer>> getColorRGBToPaletteIdx() {
		return colorRGBToPaletteIdx;
	}
	
	
	
}
