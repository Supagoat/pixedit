package q.pix.ui.button;

import java.awt.Color;

import javax.swing.JTextField;

public class PaletteColorTextInput extends JTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int luminanceIndex;
	private Color color;
	
	public PaletteColorTextInput(Color color, int luminanceIndex) {
		super(7);
		this.color = color;
		this.luminanceIndex = luminanceIndex;
	}

	public int getLuminanceIndex() {
		return luminanceIndex;
	}

	public void setLuminanceIndex(int luminanceIndex) {
		this.luminanceIndex = luminanceIndex;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	
	
}
