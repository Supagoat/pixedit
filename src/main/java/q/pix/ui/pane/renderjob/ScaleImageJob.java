package q.pix.ui.pane.renderjob;

import java.awt.Color;
import java.awt.image.BufferedImage;

import q.pix.ui.pane.GraphicsPanel;
import q.pix.ui.pane.RenderMaster;

public class ScaleImageJob implements Runnable {
	private GraphicsPanel graphicsPanel;
	private BufferedImage source;
	private BufferedImage scaled;
	private int x;
	private int y;
	private int width;
	private int height;
	private int scale;

	public ScaleImageJob(GraphicsPanel graphicsPanel, BufferedImage source, BufferedImage scaled, int x, int y,
			int width, int height, int scale) {
		setGraphicsPanel(graphicsPanel);
		setSource(source);
		setScaled(scaled);
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
		setScale(scale);
	}
	
	public void run() {
		for (int x = getX(); x < getWidth()+getX(); x++) {
			for (int y = getY(); y < getHeight()+getHeight(); y++) {
				for (int xs = 0; xs < getScale(); xs++) {
					for (int ys = 0; ys < getScale(); ys++) {
						if ((getWidth() > (getGraphicsPanel().getxView() + x)
								&& getHeight() > getGraphicsPanel().getyView() + y)
								&& ((getGraphicsPanel().getxView() + x) > -1)
								&& ((getGraphicsPanel().getyView() + y) > -1)) {
							getScaled().setRGB(x * getScale() + xs, y * getScale() + ys, Color.RED.getRGB());
									//getSource().getRGB(getGraphicsPanel().getxView() + x, getGraphicsPanel().getyView() + y));
						} else {
							getScaled().setRGB(x * getScale() + xs, y * getScale() + ys, Color.BLUE.getRGB());
						}
					}
				}
			}
		}
	}

	public GraphicsPanel getGraphicsPanel() {
		return graphicsPanel;
	}

	public ScaleImageJob setGraphicsPanel(GraphicsPanel graphicsPanel) {
		this.graphicsPanel = graphicsPanel;
		return this;
	}

	public BufferedImage getScaled() {
		return scaled;
	}

	public ScaleImageJob setScaled(BufferedImage scaled) {
		this.scaled = scaled;
		return this;
	}

	public int getX() {
		return x;
	}

	public ScaleImageJob setX(int x) {
		this.x = x;
		return this;
	}

	public int getY() {
		return y;
	}

	public ScaleImageJob setY(int y) {
		this.y = y;
		return this;
	}

	public int getWidth() {
		return width;
	}

	public ScaleImageJob setWidth(int width) {
		this.width = width;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public ScaleImageJob setHeight(int height) {
		this.height = height;
		return this;
	}

	public int getScale() {
		return scale;
	}

	public ScaleImageJob setScale(int scale) {
		this.scale = scale;
		return this;
	}

	public BufferedImage getSource() {
		return source;
	}

	public ScaleImageJob setSource(BufferedImage source) {
		this.source = source;
		return this;
	}

}