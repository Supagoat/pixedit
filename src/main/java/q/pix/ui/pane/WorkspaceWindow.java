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
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import q.pix.ui.button.DisplayModeButton;

public class WorkspaceWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private GraphicsPanel graphicsPanel;
	private DisplayMode displayMode;
	private BufferedImage inputImage;
	private BufferedImage targetImage;
	public static final int IMAGE_SIZE = 256;

	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton penIncreaseButton;
	private JButton penDecreaseButton;
	private JButton saveButton;
	private int backgroundColor;
	private boolean drawOutsideLines;
	
	private JButton activeColor;
	private JPanel colorPanel;
	private JPanel topPanel;
	
	public enum DisplayMode {
		Overlay, SideBySide
	}

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
			case 's':
				break;
			case '[':
				getPenDecreaseButton().getActionListeners()[0].actionPerformed(null);
				break;
			case ']':
				getPenIncreaseButton().getActionListeners()[0].actionPerformed(null);
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

	public WorkspaceWindow() {
		setSize(1000, 1000);// getZoomLevel()*AppState.IMAGE_SIZE+getHorizontalUISize(),
							// getZoomLevel()*AppState.IMAGE_SIZE+getVerticalUISize());
		setLayout(new BorderLayout());
		setDisplayMode(DisplayMode.SideBySide);
		setGraphicsPanel(new GraphicsPanel(this, getInputImage(), getTargetImage()));
		add(getGraphicsPanel(), BorderLayout.CENTER);
		setTopPanel(new JPanel());
		getTopPanel().setSize(1000, 20);
		getTopPanel().setLayout(new FlowLayout());
		getTopPanel().add(new DisplayModeButton(this));
		getTopPanel().add(setZoomInButton(makeZoomButton("Z+", 1)));
		getTopPanel().add(setZoomOutButton(makeZoomButton("Z-", -1)));
		getTopPanel().add(setPenIncreaseButton(makePenButton("P+", 1)));
		getTopPanel().add(setPenDecreaseButton(makePenButton("P-", -1)));
		//topPanel.add(makeDrawOutsideLinesButton());
		add(getTopPanel(), BorderLayout.PAGE_START);

		setColorPanel(new JPanel());
		getColorPanel().setSize(20, 900);
		getColorPanel().setLayout(new BoxLayout(colorPanel, BoxLayout.PAGE_AXIS));
		getColorPanel().add(makeColorButton("Head", Color.RED));
		getColorPanel().add(makeColorButton("Back Arm", Color.GREEN));
		getColorPanel().add(makeColorButton("Front Arm", Color.BLUE));
		getColorPanel().add(makeColorButton("Body", Color.ORANGE));
		getColorPanel().add(makeColorButton("Back Leg", Color.CYAN));
		getColorPanel().add(makeColorButton("Front Leg", Color.YELLOW));
		add(getColorPanel(), BorderLayout.WEST);

		addListener(topPanel);
		addListener(getColorPanel());
		addListener(getGraphicsPanel());

	}

	public void addListener(Component component) {
		component.addKeyListener(listener);
		component.setFocusable(true);
	}

	public void setInputFilePath(String path) {
		topPanel.add(makeSaveButton(path));
	}
	
	public void display() {
		setVisible(true);
	}

	public GraphicsPanel getGraphicsPanel() {
		return graphicsPanel;
	}

	public WorkspaceWindow setGraphicsPanel(GraphicsPanel graphicsPanel) {
		this.graphicsPanel = graphicsPanel;
		return this;
	}

	public BufferedImage getInputImage() {
		return inputImage;
	}

	public WorkspaceWindow setInputImage(BufferedImage inputImage) {
		this.inputImage = inputImage;
		getGraphicsPanel().setInputImage(inputImage);
		return this;
	}

	public BufferedImage getTargetImage() {
		return targetImage;
	}

	public WorkspaceWindow setTargetImage(BufferedImage targetImage) {
		this.targetImage = targetImage;
		getGraphicsPanel().setTargetImage(targetImage);
		if(deriveBackgroundColor(targetImage).isPresent()) {
			setBackgroundColor(deriveBackgroundColor(targetImage).get());
			getColorPanel().add(makeColorButton("Erase", new Color(getBackgroundColor())));
		} else {
			System.out.println("Could not derive background color");
		}
		return this;
	}

	public DisplayMode getDisplayMode() {
		return displayMode;
	}

	public WorkspaceWindow setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
		return this;
	}
	
	public Optional<Integer> deriveBackgroundColor(BufferedImage image) {
		int ul = image.getRGB(0, 0);
		if(ul == image.getRGB(image.getWidth()-1, image.getHeight()-1) &&
				ul == image.getRGB(0, image.getHeight()-1) &&
				ul == image.getRGB(image.getWidth()-1, 0)) {
			return Optional.of(ul);
		}
		return Optional.empty();
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

	private JButton makeColorButton(String label, Color color) {
		JButton colorButton = new JButton(label);
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getGraphicsPanel().setDrawColor(color);
				if(getActiveColor() != null) {
					getActiveColor().setBackground(Color.WHITE);
				}
				colorButton.setBackground(Color.GRAY);
				setActiveColor(colorButton);
			}
		});
		addListener(colorButton);
		return colorButton;
	}

	private JButton makeRefreshButton() {
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		addListener(refreshButton);
		return refreshButton;
	}
	

	private JButton makeDrawOutsideLinesButton() {
		String linesIgnored = "Lines Ignored";
		String linesRespected = "Lines Respected";
		JButton refreshButton = new JButton(linesIgnored);
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(refreshButton.getText().equals(linesIgnored)) {
					refreshButton.setText(linesRespected);
					setDrawOutsideLines(false);
					
				} else {
					refreshButton.setText(linesIgnored);
					setDrawOutsideLines(true);
				}
			}
		});
		addListener(refreshButton);
		return refreshButton;
	}


	private JButton makeSaveButton(String inputFilePath) {
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ImageIO.write(getInputImage(), "png", new File(inputFilePath));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		addListener(saveButton);
		return saveButton;
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

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public WorkspaceWindow setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public JButton getActiveColor() {
		return activeColor;
	}

	public WorkspaceWindow setActiveColor(JButton activeColor) {
		this.activeColor = activeColor;
		return this;
	}

	public JPanel getColorPanel() {
		return colorPanel;
	}

	public WorkspaceWindow setColorPanel(JPanel colorPanel) {
		this.colorPanel = colorPanel;
		return this;
	}

	public boolean isDrawOutsideLines() {
		return drawOutsideLines;
	}

	public WorkspaceWindow setDrawOutsideLines(boolean drawOutsideLines) {
		this.drawOutsideLines = drawOutsideLines;
		return this;
	}

	public JButton getSaveButton() {
		return saveButton;
	}

	public WorkspaceWindow setSaveButton(JButton saveButton) {
		this.saveButton = saveButton;
		return this;
	}

	public JPanel getTopPanel() {
		return topPanel;
	}

	public WorkspaceWindow setTopPanel(JPanel topPanel) {
		this.topPanel = topPanel;
		return this;
	}
	
	

}