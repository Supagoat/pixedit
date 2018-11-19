package q.pix.ui.pane;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import q.pix.ui.pane.WorkspaceWindow.DisplayMode;
import q.pix.util.ImageUtil;

public class GraphicsPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private WorkspaceWindow workspaceWindow;
	private int zoomLevel = 1;
	private BufferedImage inputImage;
	private BufferedImage targetImage;
	private Color drawColor = Color.GREEN;
	private int lastX,lastY;

	public GraphicsPanel(WorkspaceWindow workspaceWindow, BufferedImage inputImage, BufferedImage targetImage) {
		setWorkspaceWindow(workspaceWindow);
		setInputImage(inputImage);
		setTargetImage(targetImage);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if (getWorkspaceWindow().getDisplayMode() == DisplayMode.Overlay) {
			drawOverlay(g2);
		} else {
			drawSideBySide(g2);
		}
	}

	// public void draw(Graphics2D g2) {
	// // g2.setColor(Color.red);
	// // g2.fillRect(0, 0, 1000, 1000);
	// drawOverlay(g2);
	// }
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
			g2.drawImage(getInputImage(), ImageUtil.IMAGE_SIZE * getZoomLevel(), 0, null);
		}
	}

	public WorkspaceWindow getWorkspaceWindow() {
		return workspaceWindow;
	}

	public GraphicsPanel setWorkspaceWindow(WorkspaceWindow workspaceWindow) {
		this.workspaceWindow = workspaceWindow;
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

	public void saveInput() {
		try {
			ImageIO.write(getInputImage(), "png", new File("test.png"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getX() < WorkspaceWindow.IMAGE_SIZE && e.getY() < WorkspaceWindow.IMAGE_SIZE) {
			getInputImage().setRGB(e.getX(), e.getY(), Color.GREEN.getRGB());
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getX() < WorkspaceWindow.IMAGE_SIZE && e.getY() < WorkspaceWindow.IMAGE_SIZE) {
			getInputImage().setRGB(e.getX(), e.getY(), getDrawColor().getRGB());
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		repaint();
	}

	public Color getDrawColor() {
		return drawColor;
	}

	public GraphicsPanel setDrawColor(Color drawColor) {
		this.drawColor = drawColor;
		return this;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (e.getX() < WorkspaceWindow.IMAGE_SIZE && e.getY() < WorkspaceWindow.IMAGE_SIZE) {
			getInputImage().setRGB(e.getX(), e.getY(), getDrawColor().getRGB());
		}
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public int getLastX() {
		return lastX;
	}

	public GraphicsPanel setLastX(int lastX) {
		this.lastX = lastX;
		return this;
	}

	public int getLastY() {
		return lastY;
	}

	public GraphicsPanel setLastY(int lastY) {
		this.lastY = lastY;
		return this;
	}
	
	

}