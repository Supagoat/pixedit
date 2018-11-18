package q.pix.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JPanel;


public class GraphicsPanel extends JPanel {
	
	private JButton modeButton;
	private int zoomLevel = 1;;
	private BufferedImage inputImage;
	private BufferedImage targetImage;
	
	public GraphicsPanel(JButton modeButton, BufferedImage inputImage, BufferedImage targetImage) {
		setModeButton(modeButton);
		setInputImage(inputImage);
		setTargetImage(targetImage);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		drawOverlay(g2);
	}

//	public void draw(Graphics2D g2) {
//		// g2.setColor(Color.red);
//		// g2.fillRect(0, 0, 1000, 1000);
//		drawOverlay(g2);
//	}
//	
	private void drawOverlay(Graphics2D g2) {
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
		if (getTargetImage() != null) {
			g2.drawImage(getTargetImage(), 0, 0, null);
		}
		if (getInputImage() != null) {
			g2.drawImage(getInputImage(), 0, 0, null);
		}
	}
	
	private void drawSideBySide(Graphics2D g2) {
		if (getTargetImage() != null) {
			g2.drawImage(getTargetImage(), 0, 0, null);
		}
		if (getInputImage() != null) {
			g2.drawImage(getInputImage(), AppState.IMAGE_SIZE*getZoomLevel(), 0, null);
		}
	}

	public JButton getModeButton() {
		return modeButton;
	}

	public GraphicsPanel setModeButton(JButton modeButton) {
		this.modeButton = modeButton;
		return this;
	}

	public int getZoomLevel() {
		return zoomLevel;
	}

	public GraphicsPanel setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
		return this;
	}

	public BufferedImage getInputImage() {
		return inputImage;
	}

	public void setInputImage(BufferedImage inputImage) {
		this.inputImage = inputImage;
	}

	public BufferedImage getTargetImage() {
		return targetImage;
	}

	public void setTargetImage(BufferedImage targetImage) {
		this.targetImage = targetImage;
	}
	
	
	
	
}