package q.pix.ui.pane;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import q.pix.colorfamily.FamilyAffinity;
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
		
		// If we're in the mode that won't let us overwrite already assigned color families (Color Safe ON)
		// Then don't process the click if we accidentally click on an already assigned pixel color
		if(((ColorFamilyWindow) getWorkspaceWindow()).getColorSafeButton().getText().equals("Color Safe ON")) {
			Set<Color> selectedColor = new HashSet<>();
			selectedColor.add(new Color(getTargetImage().getTargetImage().getRGB(x, y)));
			FamilyAffinity affinity = new FamilyAffinity(selectedColor, ((ColorFamilyWindow) getWorkspaceWindow()).getColorFamily());
			if(affinity.getMissingColors(selectedColor).size() == 0) {
				return;
			}
		}
		
		if (x > -1 && x < getTargetImage().getTargetImage().getWidth() && y > -1
				&& y < getTargetImage().getTargetImage().getHeight()
				&& getTargetImage().getTargetImage().getRGB(x, y) != green.getRGB()) {
			((ColorFamilyWindow) getWorkspaceWindow())
					.assignColorGroup(new Color(getTargetImage().getTargetImage().getRGB(x, y)));
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

	@Override
	protected BufferedImage scaleInput() {
		BufferedImage img = super.scaleInput();
		if (((ColorFamilyWindow) getWorkspaceWindow()).getPixelHighlightButton().getText().equals("Normal")) {
			Set<Color> imageColors = ImageUtil.getDistinctColors(getTargetImage().getTargetImage());
			FamilyAffinity affinity = new FamilyAffinity(imageColors,
					((ColorFamilyWindow) getWorkspaceWindow()).getColorFamily());
			for (Color mc : affinity.getMissingColors(imageColors)) {
				for (Point p : getTargetImage().getColorCoords().get(mc)) {
					for (int xs = 0; xs < getZoomLevel(); xs++) {
						for (int ys = 0; ys < getZoomLevel(); ys++) {
							img.setRGB(p.x * getZoomLevel() + xs, p.y * getZoomLevel() + ys, Color.WHITE.getRGB());
						}
					}
				}
			}
		}
		return img;
	}

}
