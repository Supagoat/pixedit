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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import q.pix.colorfamily.FamilyAffinity;
import q.pix.ui.pane.WorkspaceWindow.DisplayMode;
import q.pix.util.ImageUtil;

public class ColorFamilyWindow extends JFrame implements WorkspacePaintWindow {

	private static final long serialVersionUID = 1L;
	private GraphicsPanel graphicsPanel;
	private DisplayMode displayMode;
	public static final int IMAGE_SIZE = 256;

	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton penIncreaseButton;
	private JButton penDecreaseButton;
	private JButton saveButton;
	private Optional<Integer> backgroundColor = Optional.empty();
	private boolean drawOutsideLines;

	private JButton activeColor;
	private JPanel colorPanel;
	private JPanel topPanel;
	List<Set<Color>> colorFamily = new ArrayList<>();
	private int currentColorFamily;

	List<Color> colorGroupColors;

	List<List<Set<Color>>> colorFamilyConfigs;

	private static final String FAMILY_DIVIDER = "-----";

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

	public ColorFamilyWindow(File configDir) {
		setSize(1900, 1000);
		setLayout(new BorderLayout());
		setDisplayMode(WorkspaceWindow.DisplayMode.SideBySide);
		setTopPanel(new JPanel());
		getTopPanel().setSize(1000, 20);
		getTopPanel().setLayout(new FlowLayout());
		getTopPanel().add(setZoomInButton(makeZoomButton("Z+", 1)));
		getTopPanel().add(setZoomOutButton(makeZoomButton("Z-", -1)));
		add(getTopPanel(), BorderLayout.PAGE_START);

		colorGroupColors = ImageUtil.initColorGroupColors();

		setColorPanel(new JPanel());
		getColorPanel().setSize(20, 900);
		getColorPanel().setLayout(new GridLayout(1, 1));// .setLayout(new BoxLayout(colorPanel, BoxLayout.PAGE_AXIS));
		for (int i = 0; i < 8; i++) {
			getColorPanel().add(makeColorButton("Family " + i, i, getColorGroupColors().get(i), this));
			getColorFamily().add(new HashSet<>());
		}

		addListener(topPanel);
		addListener(getColorPanel());
		add(getColorPanel(), BorderLayout.SOUTH);
	}

	public void addListener(Component component) {
		component.addKeyListener(listener);
		component.setFocusable(true);
	}

	public Optional<FamilyAffinity> loadConfigFiles(Set<Color> imageColors, File dir) {
		setColorFamilyConfigs(new ArrayList<>());

		List<FamilyAffinity> affinities = new ArrayList<>();
		try {
			for (File config : dir.listFiles()) {
				BufferedReader in = new BufferedReader(new FileReader(config));
				String line = in.readLine();
				if (line.equals(FAMILY_DIVIDER)) { // it's a config file!
					List<Set<Color>> configFamily = new ArrayList<>();
					Set<Color> currentFamily = null;
					while (line != null) {
						if (FAMILY_DIVIDER.contentEquals(line)) {
							if (currentFamily != null) {
								configFamily.add(currentFamily);
							}
							currentFamily = new HashSet<>();
						} else {
							String[] colorStr = line.split(",");
							currentFamily.add(new Color(Integer.parseInt(colorStr[0]), Integer.parseInt(colorStr[1]),
									Integer.parseInt(colorStr[2])));
						}
						line = in.readLine();
					}
					getColorFamilyConfigs().add(configFamily);
					affinities.add(ImageUtil.calculateColorGroupAffinity(imageColors, configFamily));
				}

				in.close();
			}
		} catch (Exception e) {

		}

		//Set<Color> inputColors = ImageUtil.getImageColors(getGraphicsPanel().getInputImage());

		if (affinities.size() > 0) {
			Collections.sort(affinities);
			if (affinities.get(0).isMatchingAffinity(imageColors)) {
				return Optional.of(affinities.get(0));
			}
		}

//		for (List<Set<Color>> configged : colorFamilyConfigs) {
//			Set<Color> allFamilyColors = new HashSet<>();
//			for (Set<Color> cc : configged) {
//				allFamilyColors.addAll(cc);
//			}
//			if (allFamilyColors.containsAll(inputColors)) {
//				setColorFamilies(configged);
//				redrawOutput();
//				if (affinities.size() > 0) {
//					Collections.sort(affinities);
//					if (affinities.get(0).isMatchingAffinity(inputColors)) {
//						return Optional.of(affinities.get(0));
//					}
//					return Optional.empty();
//				}
//			}
//		}

		return Optional.empty();
	}

	public void setInputFilePath(String outputPath, String inputFileName) {
		topPanel.add(makeSaveButton(getColorFamily(), outputPath, inputFileName));
	}

	public void display() {
		setVisible(true);
	}

	public GraphicsPanel getGraphicsPanel() {
		return graphicsPanel;
	}

	public ColorFamilyWindow setGraphicsPanel(GraphicsPanel graphicsPanel, File configDir) {
		this.graphicsPanel = graphicsPanel;
		add(getGraphicsPanel(), BorderLayout.CENTER);
		addListener(getGraphicsPanel());
		graphicsPanel.setZoomLevel(4);
		Optional<FamilyAffinity> affinity = loadConfigFiles(ImageUtil.getImageColors(getGraphicsPanel().getInputImage()), configDir); 
		if(affinity.isPresent()) {
			setColorFamily(affinity.get().getColorFamily());
			redrawOutput();
		}
		return this;
	}

	public ColorFamilyWindow setBackgroundColor(BufferedImage targetImage) {
//		if(ImageUtil.deriveBackgroundColor(targetImage).isPresent()) {
//			setBackgroundColor(ImageUtil.deriveBackgroundColor(targetImage).get());
//			getColorPanel().add(makeColorButton("Erase", new Color(getBackgroundColor().get())));
//		} else {
//			System.out.println("Could not derive background color");
//		}
		return this;
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

	private JButton makeSaveButton(List<Set<Color>> colorFamilies, String outputFilepath, String inputFileName) {
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					PrintWriter out = new PrintWriter(new FileWriter(new File(outputFilepath +File.separator+inputFileName.replace(".png", ".txt"))));
					for (Set<Color> family : colorFamilies) {
						out.println(FAMILY_DIVIDER);
						for (Color c : family) {
							out.println(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
						}
					}
					out.flush();
					out.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		addListener(saveButton);
		return saveButton;
	}

	public void assignColorFamily(Color c) {
		for (Set<Color> family : getColorFamily()) {
			family.remove(c);
		}
		getColorFamily().get(getCurrentColorFamily()).add(c);
		redrawOutput();
	}

	public void redrawOutput() {
		for (int y = 0; y < getGraphicsPanel().getInputImage().getHeight(); y++) {
			for (int x = 0; x < getGraphicsPanel().getInputImage().getWidth(); x++) {
				Color c = new Color(getGraphicsPanel().getTargetImage().getRGB(x, y));
				int colorIdx = ImageUtil.getColorGroupIndex(getColorFamily(), c);
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

	private JButton getPenIncreaseButton() {
		return penIncreaseButton;
	}

	private JButton setPenIncreaseButton(JButton penIncreaseButton) {
		this.penIncreaseButton = penIncreaseButton;
		return penIncreaseButton;
	}

	private JButton getPenDecreaseButton() {
		return penDecreaseButton;
	}

	private JButton setPenDecreaseButton(JButton penDecreaseButton) {
		this.penDecreaseButton = penDecreaseButton;
		return penDecreaseButton;
	}

	public Optional<Integer> getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = Optional.of(backgroundColor);
		// getColorPanel().add(makeColorButton("Erase", new
		// Color(getBackgroundColor().get())));
	}

	public JButton getActiveColor() {
		return activeColor;
	}

	public ColorFamilyWindow setActiveColor(Color color) {
		// this.currentColorFamily = Integer.parseInt(activeColor.getLabel());
		this.activeColor = activeColor;
		return this;
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

	public List<Set<Color>> getColorFamily() {
		return colorFamily;
	}

	public void setColorFamily(List<Set<Color>> colorFamily) {
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

	public List<List<Set<Color>>> getColorFamilyConfigs() {
		return colorFamilyConfigs;
	}

	public void setColorFamilyConfigs(List<List<Set<Color>>> colorFamilyConfigs) {
		this.colorFamilyConfigs = colorFamilyConfigs;
	}

}