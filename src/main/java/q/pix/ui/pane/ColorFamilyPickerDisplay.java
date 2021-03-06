package q.pix.ui.pane;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import q.pix.util.ImageUtil;

public class ColorFamilyPickerDisplay extends GraphicsPanel {
	private static final long serialVersionUID = 1L;

	public ColorFamilyPickerDisplay(ColorFamilyWindow workspaceWindow) {
		super(workspaceWindow, null, null);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public ColorFamilyPickerDisplay(ColorFamilyWindow workspaceWindow, BufferedImage inputImage) {
		super(workspaceWindow, inputImage, ImageUtil.copyImage(inputImage));
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	public void onGraphicsWindowClick(MouseEvent e) {
		int x = e.getX() / getZoomLevel() + getxView();
		int y = e.getY() / getZoomLevel() + getyView();
		Color green = new Color(0, 255, 0);
		if (x > -1 && x < getTargetImage().getWidth() && y > -1 && y < getTargetImage().getHeight()
				&& getTargetImage().getRGB(x, y) != green.getRGB()) {
			((ColorFamilyWindow) getWorkspaceWindow()).assignColorGroup(new Color(getTargetImage().getRGB(x, y)));
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
