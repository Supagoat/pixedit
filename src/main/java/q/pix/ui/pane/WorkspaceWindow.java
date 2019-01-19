package q.pix.ui.pane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	public enum DisplayMode {
		Overlay, SideBySide
	}

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
		topPanel.add(makeZoomButton("Z+", 1));
		topPanel.add(makeZoomButton("Z-", -1));
		topPanel.add(makePenButton("P+", 1));
		topPanel.add(makePenButton("P-", -1));
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

		return zoomInButton;
	}

	private JButton makePenButton(String label, int changeBy) {
		JButton zoomInButton = new JButton(label);
		zoomInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int changeTo = getGraphicsPanel().getPenSize() + changeBy;
				if (changeTo > -1) {
					getGraphicsPanel().setPenSize(changeTo);
				}
			}
		});

		return zoomInButton;
	}

	private JButton makeColorButton(String label, Color color) {
		JButton colorButton = new JButton(label);
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getGraphicsPanel().setDrawColor(color);
			}
		});

		return colorButton;
	}

	private JButton makeRefreshButton() {
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
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
		return saveButton;
	}
}