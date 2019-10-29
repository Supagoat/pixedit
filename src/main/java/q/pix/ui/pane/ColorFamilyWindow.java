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
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import q.pix.ui.pane.WorkspaceWindow.DisplayMode;

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
	List<Set<Color>> colorFamilies = new ArrayList<>();
	private int currentColorFamily;
	
	List<Color> colorGroupColors;

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
		setSize(1900, 1000);// getZoomLevel()*AppState.IMAGE_SIZE+getHorizontalUISize(),
							// getZoomLevel()*AppState.IMAGE_SIZE+getVerticalUISize());
		setLayout(new BorderLayout());
		setDisplayMode(WorkspaceWindow.DisplayMode.SideBySide);
		setTopPanel(new JPanel());
		getTopPanel().setSize(1000, 20);
		getTopPanel().setLayout(new FlowLayout());
		getTopPanel().add(setZoomInButton(makeZoomButton("Z+", 1)));
		getTopPanel().add(setZoomOutButton(makeZoomButton("Z-", -1)));
		//topPanel.add(makeDrawOutsideLinesButton());
		add(getTopPanel(), BorderLayout.PAGE_START);

		initColorGroupColors();
		
		setColorPanel(new JPanel());
		getColorPanel().setSize(20, 900);
		getColorPanel().setLayout(new GridLayout(1, 1));//.setLayout(new BoxLayout(colorPanel, BoxLayout.PAGE_AXIS));
		for(int i=0;i<8;i++) {
			getColorPanel().add(makeColorButton("Family "+i, i, getColorGroupColors().get(i), this));
			colorFamilies.add(new HashSet<>());
		}


		addListener(topPanel);
		addListener(getColorPanel());
		add(getColorPanel(), BorderLayout.SOUTH);
	}

	public void addListener(Component component) {
		component.addKeyListener(listener);
		component.setFocusable(true);
	}

	private void initColorGroupColors() {
		colorGroupColors = new ArrayList<>();
		Color gc = new Color(251, 0, 251);
		for(int i=1;i<9;i++) {
			int r = (i%3 == 0) ?  gc.getRed()-100 : gc.getRed();
			int g = (i%3 == 1) ? gc.getGreen()+100 : gc.getGreen() ;
			int b = (i%3 == 2) ? gc.getBlue()-100 : gc.getBlue() ;
			if(r < 0) {
				r = 150;
			}
			if(g > 255) {
				g = 150;
			}
			if(b < 0) {
				b = 150;
			}
			gc = new Color(r, g, b);
			colorGroupColors.add(gc);
		}
	}
	
	public void setInputFilePath(String outputPath) {
		topPanel.add(makeSaveButton(getColorFamilies(), outputPath));
	}
	
	public void display() {
		setVisible(true);
	}

	public GraphicsPanel getGraphicsPanel() {
		return graphicsPanel;
	}

	public ColorFamilyWindow setGraphicsPanel(GraphicsPanel graphicsPanel) {
		this.graphicsPanel = graphicsPanel;
		add(getGraphicsPanel(), BorderLayout.CENTER);
		addListener(getGraphicsPanel());
		graphicsPanel.setZoomLevel(4);
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

	private JButton makePenButton(String label, int changeBy) {
		JButton penButton = new JButton(label);
		penButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int changeTo = getGraphicsPanel().getPenSize() + changeBy;
				if (changeTo > -1) {
					getGraphicsPanel().setPenSize(changeTo);
				}
			}
		});
		addListener(penButton);
		return penButton;
	}

	private JButton makeColorButton(String label, int family, Color color, ColorFamilyWindow window) {
		JButton colorButton = new JButton(label);
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.setCurrentColorFamily(family);
				colorButton.setBackground(color);
				colorButton.setForeground(color);
				//setActiveColor(colorButton);
			}
		});
		addListener(colorButton);
		return colorButton;
	}

	private JButton makeSaveButton(List<Set<Color>> colorFamilies, String outputFilepath) {
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					PrintWriter out = new PrintWriter(new FileWriter(new File(outputFilepath+".txt")));
					for(Set<Color> family : colorFamilies) {
						out.println("-----");
						for(Color c : family) {
							out.println(c.getRGB());
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
		for(Set<Color> family : getColorFamilies()) {
			family.remove(c);
		}
		getColorFamilies().get(getCurrentColorFamily()).add(c);
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
		//getColorPanel().add(makeColorButton("Erase", new Color(getBackgroundColor().get())));
	}

	public JButton getActiveColor() {
		return activeColor;
	}

	public ColorFamilyWindow setActiveColor(Color color) {
		//this.currentColorFamily = Integer.parseInt(activeColor.getLabel());
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

	public List<Set<Color>> getColorFamilies() {
		return colorFamilies;
	}

	public ColorFamilyWindow setColorFamilies(List<Set<Color>> colorFamilies) {
		this.colorFamilies = colorFamilies;
		return this;
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
	
	
	

}