package q.pix.ui.pane;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ColorFamilyPickerDisplay extends GraphicsPanel {
	private static final long serialVersionUID = 1L;

	public ColorFamilyPickerDisplay(ColorFamilyWindow workspaceWindow, BufferedImage inputImage,
			BufferedImage targetImage) {
		super(workspaceWindow, inputImage, targetImage);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	public void onGraphicsWindowClick(MouseEvent e) {
		int x = e.getX() / getZoomLevel() + getxView();
		int y = e.getY() / getZoomLevel() + getyView();
		if (x > -1 && x < getInputImage().getWidth() && y > -1 && y < getInputImage().getHeight()) {
			((ColorFamilyWindow) getWorkspaceWindow()).assignColorFamily(new Color(getInputImage().getRGB(x, y)));
			super.onGraphicsWindowClick(e);
		}
	}

	@Override
	public void mouseEvent(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
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

}
