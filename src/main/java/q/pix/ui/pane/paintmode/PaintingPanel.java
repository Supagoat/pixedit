package q.pix.ui.pane.paintmode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;

import q.pix.ui.pane.GraphicsPanel;
import q.pix.ui.pane.WorkspacePaintWindow;
import q.pix.ui.pane.WorkspaceWindow;

public class PaintingPanel extends GraphicsPanel {
	private static final long serialVersionUID = 1L;
	private long bgSelectTime = Long.MAX_VALUE;
	
	public PaintingPanel(WorkspacePaintWindow workspaceWindow, BufferedImage inputImage, BufferedImage targetImage) {
		super(workspaceWindow, inputImage, targetImage);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void paintPixels(int x, int y) {
		for (int iterX = 0; iterX < getPenSize(); iterX++) {
			for (int iterY = 0; iterY < getPenSize(); iterY++) {
				int pixX = (x / getZoomLevel()) + iterX;
				int pixY = (y / getZoomLevel()) + iterY;
				if (pixX > -1 && pixY > -1 && pixX < WorkspaceWindow.IMAGE_SIZE && pixY < WorkspaceWindow.IMAGE_SIZE) {
					// if(!getWorkspaceWindow().isDrawOutsideLines() && getTargetImage().getRGB(pixX
					// + getxView(), pixY + getyView()) ==
					// getWorkspaceWindow().getBackgroundColor()) {
					// continue;
					// }
					try {
						getInputImage().setRGB(pixX + getxView(), pixY + getyView(), getDrawColor().getRGB());
					} catch (Exception e) {
						System.out.println("Out of bounds at " + x + "," + y);
					}
				}
			}
		}
	}

	public void overlayPaintbrush(MouseEvent e) {
		repaint();
		Graphics2D g2 = (Graphics2D) getGraphics();
		g2.setColor(Color.red);
		g2.fillRect(e.getX(), e.getY(), getPenSize() * getZoomLevel(), getPenSize() * getZoomLevel());
		// TODO: make the overlay draw happen when needed... Figure out when the image
		// isn't actualy displayed... drawOverlay(g2);
	}

	public void overlayBackgroundSelect(MouseEvent e) {
		repaint();
		Graphics2D g2 = (Graphics2D) getGraphics();
		g2.setColor(Color.blue);
		g2.drawOval(e.getX(), e.getY(), 8, 8);
		// TODO: make the overlay draw happen when needed... Figure out when the image
		// isn't actualy displayed... drawOverlay(g2);
	}

	@Override
	public void onGraphicsWindowClick(MouseEvent e) {
		if (getPressedButtons()[0]) {
			paintPixels(e.getX(), e.getY());
		}
		super.onGraphicsWindowClick(e);
	}

	@Override
	public void mouseEvent(MouseEvent e) {
		if (!getWorkspaceWindow().getBackgroundColor().isPresent()) {
			overlayBackgroundSelect(e);
			if (e.getButton() != MouseEvent.NOBUTTON && !getWorkspaceWindow().getBackgroundColor().isPresent()) {
				getWorkspaceWindow().setBackgroundColor(getTargetImage().getRGB(e.getX() / getZoomLevel() + getxView(),
						e.getY() / getZoomLevel() + getyView()));
				setBgSelectTime(System.currentTimeMillis());
			}
			return;
		} else {
			overlayPaintbrush(e);
		}
		// 2 second delay before it'll listen to other inputs otherwise it just starts painting
		if(System.currentTimeMillis()-getBgSelectTime() > 2000) {
			super.mouseEvent(e);
		}
	}

	protected long getBgSelectTime() {
		return bgSelectTime;
	}

	protected PaintingPanel setBgSelectTime(long bgSelectTime) {
		this.bgSelectTime = bgSelectTime;
		return this;
	}

}