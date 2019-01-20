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

	public enum DisplayMode {
		Overlay, SideBySide
	}

	private KeyListener listener = new KeyListener() {
		@Override
		public void keyPressed(KeyEvent arg0) {
			switch (arg0.getKeyChar()) {
			case '-':
				getZoomOutButton().getActionListeners()[0].actionPerformed(null);
				System.out.println("AAAA");
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
		JPanel topPanel = new JPanel();
		topPanel.setSize(1000, 20);
		topPanel.setLayout(new FlowLayout());
		topPanel.add(new DisplayModeButton(this));
		topPanel.add(setZoomInButton(makeZoomButton("Z+", 1)));
		topPanel.add(setZoomOutButton(makeZoomButton("Z-", -1)));
		topPanel.add(setPenIncreaseButton(makePenButton("P+", 1)));
		topPanel.add(setPenDecreaseButton(makePenButton("P-", -1)));
		topPanel.add(makeRefreshButton());
		add(topPanel, BorderLayout.PAGE_START);

		JPanel colorPanel = new JPanel();
		colorPanel.setSize(20, 900);
		colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.PAGE_AXIS));
		colorPanel.add(makeColorButton("Head", Color.RED));
		colorPanel.add(makeColorButton("Back Arm", Color.GREEN));
		colorPanel.add(makeColorButton("Front Arm", Color.BLUE));
		colorPanel.add(makeColorButton("Body", Color.ORANGE));
		colorPanel.add(makeColorButton("Back Leg", Color.CYAN));
		colorPanel.add(makeColorButton("Front Leg", Color.YELLOW));
		add(colorPanel, BorderLayout.WEST);

		addListener(topPanel);
		addListener(colorPanel);
		addListener(getGraphicsPanel());
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
		return this;
	}

	public DisplayMode getDisplayMode() {
		return displayMode;
	}

	public WorkspaceWindow setDisplayMode(DisplayMode displayMode) {
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

	private JButton makeColorButton(String label, Color color) {
		JButton colorButton = new JButton(label);
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getGraphicsPanel().setDrawColor(color);
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

	private JButton makeSaveButton() {
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ImageIO.write(getInputImage(), "png", new File("test.png"));
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

}