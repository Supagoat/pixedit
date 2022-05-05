package q.pix.ui.pane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import com.fasterxml.jackson.databind.ObjectMapper;

import q.pix.colorfamily.TargetImage;
import q.pix.obj.Palette;
import q.pix.ui.pane.WorkspaceWindow.DisplayMode;
import q.pix.ui.pane.paintmode.PaintingPanel;
import q.pix.util.ImageUtil;

public class PaletteAssemblyWindow extends JFrame implements WorkspacePaintWindow {

	private static final long serialVersionUID = 1L;
	private GraphicsPanel graphicsPanel;
	private DisplayMode displayMode;
	public static final int IMAGE_SIZE = 256;

	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton saveButton;
	private JButton openButton;

	private Optional<Integer> backgroundColor = Optional.empty();

	private JPanel colorPanel;
	private JPanel topPanel;
	private JPanel leftPanel;
	private JPanel bottomPanel;
	private JPanel rightPanel;

	private Palette palette;
	private Map<String, BufferedImage> images;

	private String paletteOutputPath;

	private BufferedImage selectedPartImg;
	private String selectPartImgName;
	private BufferedImage composedPartsImg;
	private PaintingPanel paintingPanel;

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

	public PaletteAssemblyWindow() {
		setSize(1900, 1000);
		setLayout(new BorderLayout());
		setDisplayMode(WorkspaceWindow.DisplayMode.SideBySide);
		setTopPanel(new JPanel());
		getTopPanel().setSize(1000, 20);
		getTopPanel().setLayout(new FlowLayout());
		getTopPanel().add(setOpenButton(makeOpenButton(this)));
		getTopPanel().add(setSaveButton(makeSaveButton()));
		getTopPanel().add(setZoomInButton(makeZoomButton("Z+", 1)));
		getTopPanel().add(setZoomOutButton(makeZoomButton("Z-", -1)));
		add(getTopPanel(), BorderLayout.PAGE_START);

		setLeftPanel(new JPanel());
		getLeftPanel().setSize(80, 800);
		getLeftPanel().setVisible(true);
		getLeftPanel().setLayout(new BoxLayout(getLeftPanel(), BoxLayout.Y_AXIS));
		add(getLeftPanel(), BorderLayout.WEST);

		setBottomPanel(new JPanel());
		getBottomPanel().setSize(1000, 20);
		getBottomPanel().setVisible(true);
		getBottomPanel().setLayout(new BoxLayout(getBottomPanel(), BoxLayout.X_AXIS));
		add(getBottomPanel(), BorderLayout.SOUTH);

		setRightPanel(new JPanel());
		getRightPanel().setSize(80, 800);
		getRightPanel().setVisible(true);
		getRightPanel().setLayout(new BoxLayout(getRightPanel(), BoxLayout.Y_AXIS));
		add(getRightPanel(), BorderLayout.EAST);

		// add(getColorPanel(), BorderLayout.SOUTH);
//		setColorPanel(new JPanel());
//		getColorPanel().setSize(20, 900);
//		getColorPanel().setLayout(new GridLayout(1, 1));

		addListener(topPanel);

		setGraphicsPanel(new GraphicsPanel(this));
		repaint();
		// add(getGraphicsPanel(), BorderLayout.CENTER);
		// addListener(getColorPanel());
		// add(getColorPanel(), BorderLayout.SOUTH);

		// setGraphicsPanel(new ColorFamilyPickerDisplay(this));
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
//
//	private JButton makeColorButton(String label, int family, Color color) {
//		JButton colorButton = new JButton(label);
//		colorButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				colorButton.setBackground(color);
//				colorButton.setForeground(color);
//			}
//		});
//		addListener(colorButton);
//		return colorButton;
//	}

	public PaletteAssemblyWindow setGraphicsPanel(GraphicsPanel newPanel) {

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

	public void setNewImage(BufferedImage inputImage) {

		getGraphicsPanel().setInputImage(inputImage);
		getGraphicsPanel().setTargetImage(new TargetImage(ImageUtil.copyImage(inputImage)));

		// dispPanel.setBackgroundColor(inputImage); // I SHOULDUSE THIS FOR THE GREEN

		redrawOutput();
	}

	public DisplayMode getDisplayMode() {
		return displayMode;
	}

	public PaletteAssemblyWindow setDisplayMode(DisplayMode displayMode) {
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

	public JButton makeOpenButton(PaletteAssemblyWindow parentWindow) {
		JButton openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(parentWindow);
				File featuredFamilyPart = fc.getSelectedFile();

				try {
					loadImages(featuredFamilyPart);

					parentWindow.setPaletteOutputPath(setupPalette(featuredFamilyPart));
					makeImageComponentSelectButtons();

					// setSelectedPartImg(getImages().get(getImages().keySet().iterator().next()));
					// // This is the image on
					// the right //));
					setComposedPartsImg(composeMasked());
					parentWindow.redrawOutput();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		return openButton;
	}

	private void makeImageComponentSelectButtons() {
		getLeftPanel().removeAll();
		for (int i = 0; i < getImages().size(); i++) {
			getLeftPanel().add(makeImageComponentSelectButton(i));// new JButton("B" + i));
		}
		getLeftPanel().revalidate();
	}

	private void makePaletteColorButtons() {
		getBottomPanel().removeAll();
		for (Color yellowColor : ImageUtil.getImageColors(getSelectedPartImg())) {
			JTextField cb = makePaletteColorChooseButton(yellowColor.getRGB());
			getBottomPanel().add(cb);
		}
		getBottomPanel().revalidate();
	}

	private JTextField makePaletteColorChooseButton(int yellowColor) {
		JTextField colorInput = new JTextField(7);
		// cButton.setBackground(new Color(yellowColor));
		colorInput.setForeground(new Color(yellowColor));
		colorInput.setText("#"+Integer.toHexString(yellowColor).substring(2));
		colorInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				    String text = colorInput.getText();
				    //textArea.append(text + newline);
				    //textField.selectAll();
				    Color newColor = null;
				    try {
				    	newColor = Color.decode(text);
				    } catch(Exception ex) {}
				///Color newColor = JColorChooser.showDialog(PaletteAssemblyWindow.this, "Choose color", Color.RED);
				if (newColor != null) {
					getPalette().setPaletteColor(getSelectPartImgName(), yellowColor,
							newColor.getRGB());
					setComposedPartsImg(composeMasked());
					PaletteAssemblyWindow.this.redrawOutput();
				}
			}

		});
		return colorInput;
	}

	private JButton makeImageComponentSelectButton(int idx) {
		JButton cButton = new JButton("" + idx);
		cButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Iterator<String> it = getImages().keySet().iterator();
				String key = null;
				for (int i = 0; i <= idx; i++) {
					key = it.next();
				}
				setSelectedPartImg(getImages().get(key));
				setSelectPartImgName(key);
				makePaletteColorButtons();
			}

		});
		return cButton;
	}

	private BufferedImage composeMasked() {
		// TODO: add the palette adjusted colors
		BufferedImage composed = null;
		for (String imgName : getImages().keySet()) {
			BufferedImage img = getImages().get(imgName);
			composed = composed == null
					? new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB)
					: composed;
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int imgColor = img.getRGB(x, y);
					int alpha = (imgColor & 0xFF000000);
					if (alpha != 0) {
						Optional<Integer> paletteRGB = getPalette().getColorAtPaletteIdx(imgName, imgColor);
						composed.setRGB(x, y, paletteRGB.isPresent() ? paletteRGB.get() : imgColor);
					}
				}
			}
		}
		return composed;
	}

	private void loadImages(File imgPathExample) {
		try {
			Map<String, BufferedImage> imgs = new TreeMap<>();

			FilenameFilter filter = (File dir, String name) -> name.contains("png")
					&& name.startsWith(getBaseOutputFileName(imgPathExample.getName()));
			File[] matches = imgPathExample.getParentFile().listFiles(filter);
			for (File match : matches) {
				// No need to do this masking because I already do it as part of my image
				// pipeline in the cleaning button
				// BufferedImage output = ImageIO.read(match);
				// BufferedImage input = ImageIO.read(new
				// File(match.getAbsolutePath().replace("-outputs", "-inputs")));

				imgs.put(match.getName(), ImageIO.read(match));// .getName(), mask(input, output));
			}
			this.images = imgs;

			// setPaletteOutputPath(setupPalette(imgPathExample));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

// No actual need for this at the moment.
	private BufferedImage mask(BufferedImage input, BufferedImage output) {
		BufferedImage masked = new BufferedImage(output.getWidth(), output.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int maskColor = ImageUtil.FAMILY_FEATURE_COLOR.getRGB();
		for (int y = 0; y < output.getHeight(); y++) {
			for (int x = 0; x < output.getWidth(); x++) {
				if (input.getRGB(x, y) == maskColor) {
					masked.setRGB(x, y, output.getRGB(x, y));
				} else {
					masked.setRGB(x, y, 0);
				}
			}
		}
		return masked;
	}

	/**
	 * Loads the palette from a file
	 * 
	 * @param featuredFamilyPart One of the image files to base the palette file
	 *                           name off of
	 * @return absolute path to where the pilot should be saved
	 */
	private String loadPalette(File featuredFamilyPart) {
		File outDir = new File(featuredFamilyPart.getParentFile().getAbsolutePath() + File.separator + "palettes");
		if (!outDir.exists()) {
			outDir.mkdir();
		}
		String paletteFilePath = outDir + File.separator + toPaletteFileName(featuredFamilyPart.getName());
		if (new File(paletteFilePath).exists()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				setPalette(mapper.readValue(new File(paletteFilePath), Palette.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			setPalette(new Palette());
		}
		return paletteFilePath;
	}

	/**
	 * Calls the loading of the palette from a file then fills in the remaining
	 * unassigned colors found in the input images mapping them to themselves
	 * 
	 * @param featuredFamilyPart One of the image files to base the palette file
	 *                           name off of
	 * @return absolute path to where the pilot should be saved
	 */
	private String setupPalette(File featuredFamilyPart) {
		String paletteFilePath = loadPalette(featuredFamilyPart);
//		for(String imgName : getImages().keySet()) {
//			SortedSet<Color> colors = ImageUtil.getDistinctColors(getImages().get(imgName));
//			for(Color c : colors) {
//
//			}
//		}
		return paletteFilePath;
	}

	private String getBaseOutputFileName(String imageFileName) {
		return imageFileName.substring(0, imageFileName.lastIndexOf('_'));
	}

	private String toPaletteFileName(String imageFileName) {
		String name = getBaseOutputFileName(imageFileName);
		return name + "_palette.txt";
	}

	public JButton makeSaveButton() {
		JButton saveButton = new JButton("Save");
//		if(getSaveButton() != null) {
//			getTopPanel().remove(getSaveButton());
//		}
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ObjectMapper mapper = new ObjectMapper();
				File outFile = new File(getPaletteOutputPath());

				try {
					mapper.writeValue(outFile, getPalette());
				} catch (Exception ex) {

					ex.printStackTrace();
				}
				// FileUtil.writePalette(palette, outputFilepath, outputFilepath);
			}
		});
		addListener(saveButton);
		return saveButton;
	}

	public void redrawOutput() {
		repaint();
		if (getGraphicsPanel().getInputImage() == null || getGraphicsPanel().getTargetImage() == null) {
			return;
		}
		/*
		 * for (int y = 0; y < getGraphicsPanel().getInputImage().getHeight(); y++) {
		 * for (int x = 0; x < getGraphicsPanel().getInputImage().getWidth(); x++) {
		 * 
		 * getGraphicsPanel().getInputImage().setRGB(x, y, c.getRGB()); } }
		 */

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

	public JPanel getColorPanel() {
		return colorPanel;
	}

	public PaletteAssemblyWindow setColorPanel(JPanel colorPanel) {
		this.colorPanel = colorPanel;
		return this;
	}

	public JButton getSaveButton() {
		return saveButton;
	}

	public JButton setSaveButton(JButton saveButton) {
		this.saveButton = saveButton;
		return saveButton;
	}

	public JPanel getTopPanel() {
		return topPanel;
	}

	public JPanel setTopPanel(JPanel topPanel) {
		this.topPanel = topPanel;
		return topPanel;
	}

	public JButton getOpenButton() {
		return openButton;
	}

	public JButton setOpenButton(JButton openButton) {
		this.openButton = openButton;
		return openButton;
	}

	public Palette getPalette() {
		return palette;
	}

	public void setPalette(Palette palette) {
		this.palette = palette;
	}

	public String getPaletteOutputPath() {
		return paletteOutputPath;
	}

	public void setPaletteOutputPath(String paletteOutputPath) {
		this.paletteOutputPath = paletteOutputPath;
	}

	public BufferedImage getSelectedPartImg() {
		return selectedPartImg;
	}

	public void setSelectedPartImg(BufferedImage selectedPartImg) {
		this.selectedPartImg = selectedPartImg;
		getGraphicsPanel().setInputImage(selectedPartImg);
		getGraphicsPanel().repaint();
	}

	public BufferedImage getComposedPartsImg() {
		return composedPartsImg;
	}

	public void setComposedPartsImg(BufferedImage composedPartsImg) {
		this.composedPartsImg = composedPartsImg;

		getGraphicsPanel().setTargetImage(new TargetImage(composedPartsImg));
	}

	public PaintingPanel getPaintingPanel() {
		return paintingPanel;
	}

	public PaintingPanel setPaintingPanel(PaintingPanel paintingPanel) {
		this.paintingPanel = paintingPanel;
		return paintingPanel;
	}

	public Map<String, BufferedImage> getImages() {
		return images;
	}

	public void setImages(Map<String, BufferedImage> images) {
		this.images = images;
	}

	public JPanel getLeftPanel() {
		return leftPanel;
	}

	public void setLeftPanel(JPanel leftPanel) {
		this.leftPanel = leftPanel;
	}

	public String getSelectPartImgName() {
		return selectPartImgName;
	}

	public void setSelectPartImgName(String selectPartImgName) {
		this.selectPartImgName = selectPartImgName;
	}

	public JPanel getBottomPanel() {
		return bottomPanel;
	}

	public void setBottomPanel(JPanel bottomPanel) {
		this.bottomPanel = bottomPanel;
	}

	public JPanel getRightPanel() {
		return rightPanel;
	}

	public void setRightPanel(JPanel rightPanel) {
		this.rightPanel = rightPanel;
	}

}