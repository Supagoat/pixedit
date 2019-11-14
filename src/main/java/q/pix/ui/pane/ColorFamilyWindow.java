package q.pix.ui.pane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import q.pix.colorfamily.ColorFamily;
import q.pix.colorfamily.FamilyAffinity;
import q.pix.ui.pane.WorkspaceWindow.DisplayMode;
import q.pix.util.FileUtil;
import q.pix.util.ImageUtil;

public class ColorFamilyWindow extends JFrame implements WorkspacePaintWindow {

	private static final long serialVersionUID = 1L;
	private GraphicsPanel graphicsPanel;
	private DisplayMode displayMode;
	public static final int IMAGE_SIZE = 256;

	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton saveButton;
	private Optional<Integer> backgroundColor = Optional.empty();
	private boolean drawOutsideLines;

	private JButton activeColor;
	private JPanel colorPanel;
	private JPanel topPanel;
	ColorFamily colorFamily = new ColorFamily();
	private int currentColorFamily;
	private Consumer<Boolean> onSaveCallback;
	List<Color> colorGroupColors;
	private List<JButton> colorFamilyButtons;

	private KeyListener listener = new KeyListener() {
		@Override
		public void keyPressed(KeyEvent arg0) {
			switch (arg0.getKeyChar()) {
			case '-':
				getZoomOutButton().getActionListeners()[0].actionPerformed(null);
				break;
			case '=':
				getZoomInButton().getActionListeners()[0].actionPerformed(null);
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent arg0) {

		}
	};

	public ColorFamilyWindow() {
		setSize(1900, 1000);
		setLayout(new BorderLayout());
		setDisplayMode(WorkspaceWindow.DisplayMode.SideBySide);
		setTopPanel(new JPanel());
		getTopPanel().setSize(1000, 20);
		getTopPanel().setLayout(new FlowLayout());
		getTopPanel().add(setZoomInButton(makeZoomButton("Z+", 1)));
		getTopPanel().add(setZoomOutButton(makeZoomButton("Z-", -1)));
		add(getTopPanel(), BorderLayout.PAGE_START);
		colorFamilyButtons = new ArrayList<>();
		colorGroupColors = ImageUtil.initColorGroupColors();

		setColorPanel(new JPanel());
		getColorPanel().setSize(20, 900);
		getColorPanel().setLayout(new GridLayout(1, 1));
		for (int i = 0; i < ColorFamily.FAMILY_GROUP_COUNT; i++) {
			getColorPanel().add(makeColorButton("Family " + i, i, getColorGroupColors().get(i), this));
			getColorFamily().addGroup(new HashSet<>());
		}
		addListener(topPanel);
		addListener(getColorPanel());
		add(getColorPanel(), BorderLayout.SOUTH);

		setGraphicsPanel(new ColorFamilyPickerDisplay(this));
	}

	public void addListener(Component component) {
		component.addKeyListener(listener);
		component.setFocusable(true);
	}

	public void display() {
		setVisible(true);
	}

	public GraphicsPanel getGraphicsPanel() {
		return graphicsPanel;
	}

	public ColorFamilyWindow setGraphicsPanel(GraphicsPanel newPanel) {

		if (getGraphicsPanel() == null) {
			this.graphicsPanel = newPanel;

			newPanel.setVisible(true);
			add(newPanel, BorderLayout.CENTER);
			addListener(newPanel);
			newPanel.setZoomLevel(4);
		} 
		redrawOutput();

		return this;
	}

	public void setNewImage(BufferedImage inputImage, ColorFamily family) {

		setColorFamily(family);
		getGraphicsPanel().setInputImage(inputImage);
		getGraphicsPanel().setTargetImage(ImageUtil.copyImage(inputImage));

		// dispPanel.setBackgroundColor(inputImage); // I SHOULDUSE THIS FOR THE GREEN

		redrawOutput();
	}

	public void setFamilyOfAffinity(FamilyAffinity affinity) {
		setColorFamily(affinity.getColorFamily());
		redrawOutput();
	}

	public DisplayMode getDisplayMode() {
		return displayMode;
	}

	public ColorFamilyWindow setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
		return this;
	}

	private JButton makeZoomButton(String label, int changeBy) {
		JButton zoomInButton = new JButton(label);
		zoomInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int changeTo = getGraphicsPanel().getZoomLevel() + changeBy;
				if (changeTo > -1) {
					getGraphicsPanel().setZoomLevel(changeTo);
					repaint();
				}
			}
		});
		addListener(zoomInButton);
		return zoomInButton;
	}

	private JButton makeColorButton(String label, int family, Color color, ColorFamilyWindow window) {
		JButton colorButton = new JButton(label);
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.setCurrentColorFamily(family);
				colorButton.setBackground(color);
				colorButton.setForeground(color);
			}
		});
		addListener(colorButton);
		return colorButton;
	}

	public JButton makeSaveButton(Supplier<ColorFamily> colorFamily, String outputFilepath,
			Supplier<String> inputFileName, Consumer<Boolean> callback) {
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtil.writeFamilyConfigFile(colorFamily.get(), outputFilepath, inputFileName.get());
				callback.accept(true);
			}
		});
		addListener(saveButton);
		topPanel.add(saveButton);
		return saveButton;
	}

	public void assignColorGroup(Color c) {
		for (Set<Color> group : getColorFamily().getColorGroups()) {
			group.remove(c);
		}
		getColorFamily().get(getCurrentColorFamily()).add(c);
		
		Set<Color> imageColors = ImageUtil.getDistinctColors(getGraphicsPanel().getTargetImage());
		FamilyAffinity affinity = new FamilyAffinity(imageColors, getColorFamily());
		System.out.println("Still to go: "+affinity.getMissingColors(imageColors));
		
		redrawOutput();
	}

	public void redrawOutput() {
		if (getGraphicsPanel().getInputImage() == null || getGraphicsPanel().getTargetImage() == null) {
			return;
		}
		for (int y = 0; y < getGraphicsPanel().getInputImage().getHeight(); y++) {
			for (int x = 0; x < getGraphicsPanel().getInputImage().getWidth(); x++) {
				Color c = new Color(getGraphicsPanel().getTargetImage().getRGB(x, y));
				int colorIdx = ImageUtil.getColorGroupIndex(getColorFamily(), c);
				
				if (colorIdx > 8) {
					c = getColorGroupColors().get(colorIdx);
				}
				if (colorIdx > -1) {
					c = getColorGroupColors().get(colorIdx);
				}

				
				getGraphicsPanel().getInputImage().setRGB(x, y, c.getRGB());
			}
		}
		repaint();
	}

	private JButton getZoomInButton() {
		return zoomInButton;
	}

	private JButton setZoomInButton(JButton zoomInButton) {
		this.zoomInButton = zoomInButton;
		return zoomInButton;
	}

	private JButton getZoomOutButton() {
		return zoomOutButton;
	}

	private JButton setZoomOutButton(JButton zoomOutButton) {
		this.zoomOutButton = zoomOutButton;
		return zoomOutButton;
	}

	public Optional<Integer> getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = Optional.of(backgroundColor);
	}

	public JButton getActiveColor() {
		return activeColor;
	}

	public JPanel getColorPanel() {
		return colorPanel;
	}

	public ColorFamilyWindow setColorPanel(JPanel colorPanel) {
		this.colorPanel = colorPanel;
		return this;
	}

	public boolean isDrawOutsideLines() {
		return drawOutsideLines;
	}

	public ColorFamilyWindow setDrawOutsideLines(boolean drawOutsideLines) {
		this.drawOutsideLines = drawOutsideLines;
		return this;
	}

	public JButton getSaveButton() {
		return saveButton;
	}

	public ColorFamilyWindow setSaveButton(JButton saveButton) {
		this.saveButton = saveButton;
		return this;
	}

	public JPanel getTopPanel() {
		return topPanel;
	}

	public ColorFamilyWindow setTopPanel(JPanel topPanel) {
		this.topPanel = topPanel;
		return this;
	}

	public ColorFamily getColorFamily() {
		return colorFamily;
	}

	public void setColorFamily(ColorFamily colorFamily) {
		this.colorFamily = colorFamily;
	}

	public List<Color> getColorGroupColors() {
		return colorGroupColors;
	}

	public void setColorGroupColors(List<Color> colorGroupColors) {
		this.colorGroupColors = colorGroupColors;
	}

	public int getCurrentColorFamily() {
		return currentColorFamily;
	}

	public void setCurrentColorFamily(int currentColorFamily) {
		this.currentColorFamily = currentColorFamily;
	}

	public Consumer<Boolean> getOnSaveCallback() {
		return onSaveCallback;
	}

	public void setOnSaveCallback(Consumer<Boolean> onSaveCallback) {
		this.onSaveCallback = onSaveCallback;
	}

	public List<JButton> getColorFamilyButtons() {
		return colorFamilyButtons;
	}

	public void setColorFamilyButtons(List<JButton> colorFamilyButtons) {
		this.colorFamilyButtons = colorFamilyButtons;
	}
	
	

}