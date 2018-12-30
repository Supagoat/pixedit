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
	private int penSize = 4;
	private int lastX, lastY;
	private int xView, yView;
	private boolean[] pressedButtons;

	public GraphicsPanel(WorkspaceWindow workspaceWindow, BufferedImage inputImage, BufferedImage targetImage) {
		setWorkspaceWindow(workspaceWindow);
		setInputImage(inputImage);
		setTargetImage(targetImage);
		addMouseListener(this);
		addMouseMotionListener(this);
		pressedButtons = new boolean[3];
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

	private void drawOverlay(Graphics2D g2) {
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
		if (getTargetImage() != null) {
			g2.drawImage(scaleImage(getTargetImage(), getZoomLevel()), 0, 0, null);
		}
		if (getInputImage() != null) {
			g2.drawImage(scaleImage(getInputImage(), getZoomLevel()), 0, 0, null);
		}
	}

	private void drawSideBySide(Graphics2D g2) {
		if (getTargetImage() != null) {
			g2.drawImage(scaleImage(getTargetImage(), getZoomLevel()), 0, 0, null);
		}
		if (getInputImage() != null) {
			g2.drawImage(scaleImage(getInputImage(), getZoomLevel()), ImageUtil.IMAGE_SIZE, 0, null);
		}
	}

	public BufferedImage scaleImage(BufferedImage img, int scale) {
		BufferedImage scaled = new BufferedImage(img.getWidth() * scale, img.getHeight() * scale, img.getType());
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getWidth(); y++) {
				for (int xs = 0; xs < scale; xs++) {
					for (int ys = 0; ys < scale; ys++) {
						if ((img.getWidth() > (getxView() + x) && img.getHeight() > getyView() + y)
							&& ((getxView() + x) > -1) && ((getyView() + y) > -1)) {
							scaled.setRGB(x * scale + xs, y * scale + ys, img.getRGB(getxView() + x, getyView() + y));
						} else {
							scaled.setRGB(x * scale + xs, y * scale + ys, Color.WHITE.getRGB());
						}
					}
				}
			}
		}
		return scaled;
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
		mouseEvent(e);
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
		mouseEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		getPressedButtons()[e.getButton() - 1] = false;
		System.out.println("Unpressing " + e.getButton());
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
		mouseEvent(e);
	}

	public void paintPixels(int x, int y) {
		for (int iterX = 0; iterX < getPenSize(); iterX++) {
			for (int iterY = 0; iterY < getPenSize(); iterY++) {
				int pixX = (x / getZoomLevel()) + iterX;
				int pixY = (y / getZoomLevel()) + iterY;
				if (pixX > -1 && pixY > -1 && pixX < WorkspaceWindow.IMAGE_SIZE && pixY < WorkspaceWindow.IMAGE_SIZE) {
					getInputImage().setRGB(pixX, pixY, getDrawColor().getRGB());
				}
			}
		}
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseEvent(e);
	}

	public void onGraphicsWindowClick(MouseEvent e) {
		if (getPressedButtons()[0]) {
			paintPixels(e.getX(), e.getY());
		}
		if (getPressedButtons()[1]) {
			moveView(e.getX(), e.getY());
		}
		setLastX(e.getX());
		setLastY(e.getY());
	}

	public void mouseEvent(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 || getPressedButtons()[0]) {
			getPressedButtons()[0] = true;
			onGraphicsWindowClick(e);
		}
		if (getPressedButtons()[1]) {
			onGraphicsWindowClick(e);
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			initMove(e.getX(), e.getY());
		}
	}

	public void initMove(int x, int y) {
		getPressedButtons()[1] = true;
		setLastX(x);
		setLastY(y);
	}

	public void moveView(int x, int y) {
		System.out.println((getxView()) + " then " + (getxView() + getLastX() - x));
		setxView(getxView() + getLastX() - x);
		setyView(getyView() + getLastY() - y);
		setLastX(x);
		setLastY(y);
		repaint();
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

	public int getxView() {
		return xView;
	}

	public GraphicsPanel setxView(int xView) {
		this.xView = xView;
		return this;
	}

	public int getyView() {
		return yView;
	}

	public GraphicsPanel setyView(int yView) {
		this.yView = yView;
		return this;
	}

	public GraphicsPanel setLastY(int lastY) {
		this.lastY = lastY;
		return this;
	}

	public int getPenSize() {
		return penSize;
	}

	public GraphicsPanel setPenSize(int penSize) {
		this.penSize = penSize;
		return this;
	}

	public boolean[] getPressedButtons() {
		return pressedButtons;
	}

	public GraphicsPanel setPressedButtons(boolean[] pressedButtons) {
		this.pressedButtons = pressedButtons;
		return this;
	}

}