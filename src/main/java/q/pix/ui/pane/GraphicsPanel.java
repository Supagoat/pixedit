package q.pix.ui.pane;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import q.pix.ui.pane.WorkspaceWindow.DisplayMode;
import q.pix.util.ImageUtil;

public class GraphicsPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private WorkspaceWindow workspaceWindow;
	private int zoomLevel = 1;
	private BufferedImage inputImage;
	private BufferedImage targetImage;

	private BufferedImage scaledInput;
	private BufferedImage scaledTarget;

	private Color drawColor = Color.GREEN;
	private int penSize = 4;
	private int lastX, lastY;
	private int xView, yView;
	private boolean[] pressedButtons;
	private ThreadPoolExecutor threadEx;
	
	private boolean inBackgroundSelectionMode = false;

	public GraphicsPanel(WorkspaceWindow workspaceWindow, BufferedImage inputImage, BufferedImage targetImage) {
		setWorkspaceWindow(workspaceWindow);
		setInputImage(inputImage);
		setTargetImage(targetImage);
		addMouseListener(this);
		addMouseMotionListener(this);
		pressedButtons = new boolean[3];
		setThreadEx(new ThreadPoolExecutor(8, 8, 10l, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>()));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		drawSideBySide(g2);
		if (getWorkspaceWindow().getDisplayMode() == DisplayMode.Overlay) {
			drawOverlay(g2);
		}
	}

	protected void drawOverlay(Graphics2D g2) {
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2F));
		if (getTargetImage() != null) {
			g2.drawImage(scaleTarget(), 0, 0, null);
		}
		if (getInputImage() != null) {
			g2.drawImage(scaleInput(), 0, 0, null);
		}
	}

	protected void drawSideBySide(Graphics2D g2) {
		if (getTargetImage() != null) {
			g2.drawImage(scaleTarget(), 0, 0, null);
		}
		if (getInputImage() != null) {
			g2.drawImage(scaleInput(), ImageUtil.IMAGE_WIDTH * getZoomLevel(), 0, null);
		}
	}
	
	/**
	 * Creates an image out of a sub-section of another image
	 * 
	 * @param source
	 *            source image
	 * @param x
	 *            left of source to copy from
	 * @param y
	 *            top of source to copy from
	 * @param width
	 *            width to copy
	 * @param height
	 *            height to copy
	 * @return
	 */
	protected BufferedImage subImg(BufferedImage source, int x, int y, int width, int height) {
		if (source == null || x > source.getWidth() || y > source.getHeight()) {
			return null;
		}
		// for(int xSplit=0;xSplit<2;xSplit++) {
		// for(int ySplit=0;ySplit<2;xSplit++) {
		//
		// }
		// }
		// getThreadEx().execute(new Renderer());
		BufferedImage img = new BufferedImage(width, height, source.getType());
		for (int xx = 0; xx < width; xx++) {
			for (int yy = 0; yy < height; yy++) {
				int sx = xx + x;
				int sy = yy + y;
				if (sx > -1 && sy > -1 && sx < source.getWidth() && sy < source.getHeight()) {
					img.setRGB(xx, yy, source.getRGB(xx + x, yy + y));
				}
			}
		}
		return img;
	}

	protected BufferedImage scaleInput() {
		BufferedImage img = scaleImage(getInputImage(), getZoomLevel());
		setScaledInput(img);
		return img;
	}

	protected BufferedImage scaleTarget() {
		BufferedImage img = scaleImage(getTargetImage(), getZoomLevel());
		setScaledTarget(img);
		return img;
	}

	public BufferedImage scaleImage(BufferedImage img, int scale) {
		BufferedImage scaled = new BufferedImage(img.getWidth() * scale, img.getHeight() * scale, img.getType());		
		

//		RenderMaster renderMaster = new RenderMaster(getThreadEx(), 4);
//		for (int xSplit = 0; xSplit < 2; xSplit++) {
//			for (int ySplit = 0; ySplit < 2; ySplit++) {
//				ScaleImageJob job = new ScaleImageJob(this, img, scaled, (img.getWidth() / 2) * xSplit,
//						(img.getHeight() / 2) * ySplit, img.getWidth() / 2, img.getHeight() / 2, scale);
//				renderMaster.submit(job);
//			}
//		}
		//
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
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

//		synchronized (renderMaster) {
//			try {
//				renderMaster.wait();
//			} catch (InterruptedException e) {
//				throw new RuntimeException(e);
//			}
//		}
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


	@Override
	public void mouseMoved(MouseEvent e) {
		mouseEvent(e);
	}


	protected void drawImgAt(BufferedImage img, int x, int y) {
		if (img != null) {
			Graphics2D g2 = (Graphics2D) getGraphics();
			g2.drawImage(img, x, y, null);
		}
	}

	public void onGraphicsWindowClick(MouseEvent e) {
		if (getPressedButtons()[1]) {
			moveView(e.getX(), e.getY());
		}
		setLastX(e.getX());
		setLastY(e.getY());
	}

	/**
	 * [0] = x [1] = y [2] = width [3] = height
	 */
	protected int[] getImgChangeCoords(MouseEvent e) {
		int[] dim = new int[4];
		dim[0] = Math.max(0, Math.min(e.getX(), getLastX()) - 10);
		dim[1] = Math.max(0, Math.min(e.getY(), getLastY()) - 10);
		dim[2] = Math.abs(e.getX() - getLastX());
		dim[2] = (dim[2] < getPenSize() ? getPenSize() : dim[2]) + 20;
		dim[3] = Math.abs(e.getY() - getLastY());
		dim[3] = (dim[3] < getPenSize() ? getPenSize() : dim[3]) + 20;
		return dim;
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
		setLastX(e.getX());
		setLastY(e.getY());
	}

	public void initMove(int x, int y) {
		getPressedButtons()[1] = true;
		setLastX(x);
		setLastY(y);
	}

	public void moveView(int x, int y) {
		setxView(getxView() + getLastX() - x);
		setyView(getyView() + getLastY() - y);
		setLastX(x);
		setLastY(y);
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

	public BufferedImage getScaledInput() {
		return scaledInput;
	}

	public GraphicsPanel setScaledInput(BufferedImage scaledInput) {
		this.scaledInput = scaledInput;
		return this;
	}

	public BufferedImage getScaledTarget() {
		return scaledTarget;
	}

	public GraphicsPanel setScaledTarget(BufferedImage scaledTarget) {
		this.scaledTarget = scaledTarget;
		return this;
	}

	public ThreadPoolExecutor getThreadEx() {
		return threadEx;
	}

	public GraphicsPanel setThreadEx(ThreadPoolExecutor threadEx) {
		this.threadEx = threadEx;
		return this;
	}

}