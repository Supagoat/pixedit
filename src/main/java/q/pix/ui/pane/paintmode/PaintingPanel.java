package q.pix.ui.pane.paintmode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import q.pix.ui.pane.GraphicsPanel;
import q.pix.ui.pane.WorkspaceWindow;

public class PaintingPanel extends GraphicsPanel {

	
	public PaintingPanel(WorkspaceWindow workspaceWindow, BufferedImage inputImage, BufferedImage targetImage) {
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
	
	@Override
	public void onGraphicsWindowClick(MouseEvent e) {
		if (getPressedButtons()[0]) {
			paintPixels(e.getX(), e.getY());
		}
		super.onGraphicsWindowClick(e);
	}
	
	@Override
	public void mouseEvent(MouseEvent e) {
		overlayPaintbrush(e);
		super.mouseEvent(e);
	}

}