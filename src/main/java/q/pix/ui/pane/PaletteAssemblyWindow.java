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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

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

	private Palette palette;
	private Map<String, BufferedImage> images;

	private String paletteOutputPath;

	private BufferedImage selectedPartImg;
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

// this is from paintingpanel	
//	public WorkspaceWindow setGraphicsPanel(GraphicsPanel graphicsPanel) {
//		this.graphicsPanel = graphicsPanel;
//		add(getGraphicsPanel(), BorderLayout.CENTER);
//		addListener(getGraphicsPanel());
//		return this;
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
				setPaletteOutputPath(loadPalette(featuredFamilyPart));
				try {
					loadImages(featuredFamilyPart);
					setSelectedPartImg(getImages().get(getImages().keySet().iterator().next())); // This is the image on the right  //));
					setComposedPartsImg(composeMasked());
					parentWindow.redrawOutput();
					// parentWindow.add(setPaintingPanel(
					// new PaintingPanel(parentWindow, getComposedPartsImg(),
					// getSelectedPartImg())),BorderLayout.CENTER);
					// getPaintingPanel().repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		return openButton;
	}
	
	private BufferedImage composeMasked() {
		//TODO: add the palette adjusted colors
		BufferedImage composed = null;
		for(String imgName : getImages().keySet()) {
			BufferedImage img = getImages().get(imgName);
			composed = composed == null ? new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_ARGB) : composed;
			for(int y=0;y<img.getHeight();y++) {
				for(int x=0;x<img.getWidth();x++) {
					int alpha = (img.getRGB(x, y) & 0xFF000000);
					if(alpha != 0) {
						composed.setRGB(x, y, img.getRGB(x, y));
					} 
				}
			}
		}
		return composed;
	}

	private void loadImages(File imgPathExample) {
		try {
			Map<String, BufferedImage> imgs = new TreeMap<>();

			FilenameFilter filter = (File dir, String name) -> name.contains("output")
					&& name.startsWith(getBaseOutputFileName(imgPathExample.getName()));
			File[] matches = imgPathExample.getParentFile().listFiles(filter);
			for (File match : matches) {
				BufferedImage output = ImageIO.read(match);
				BufferedImage input = ImageIO.read(new File(match.getAbsolutePath().replace("-outputs", "-inputs")));
				
				imgs.put(match.getName(), mask(input, output));
			}
			this.images = imgs;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private BufferedImage mask(BufferedImage input, BufferedImage output) {
		BufferedImage masked = new BufferedImage(output.getWidth(), output.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int maskColor = ImageUtil.FAMILY_FEATURE_COLOR.getRGB();
		for(int y=0;y<output.getHeight();y++) {
			for(int x=0;x<output.getWidth();x++) {
				if(input.getRGB(x, y) == maskColor) {
					masked.setRGB(x, y, output.getRGB(x, y));
				} else {
					masked.setRGB(x, y, 0);
				}
			}
		}
		return masked;
	}

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
		}
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
	
	

}