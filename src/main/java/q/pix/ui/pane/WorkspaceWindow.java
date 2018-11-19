package q.pix.ui.pane;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;

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
	public static final int IMAGE_SIZE=256; 
	
	public enum DisplayMode {
		Overlay, SideBySide
	}
	
	public WorkspaceWindow() {
		setSize(1000,1000);//getZoomLevel()*AppState.IMAGE_SIZE+getHorizontalUISize(), getZoomLevel()*AppState.IMAGE_SIZE+getVerticalUISize());
		setLayout(new BorderLayout());
		setDisplayMode(DisplayMode.SideBySide);
		setGraphicsPanel(new GraphicsPanel(this, getInputImage(), getTargetImage()));
		add(getGraphicsPanel(), BorderLayout.CENTER);
		JPanel topPanel = new JPanel();
		topPanel.setSize(1000, 20);
		topPanel.setLayout(new FlowLayout());
		topPanel.add(new DisplayModeButton(this));
		topPanel.add(new JButton("+"));
		topPanel.add(new JButton("-"));
		add(topPanel, BorderLayout.PAGE_START);
		
		JPanel colorPanel = new JPanel();
		colorPanel.setSize(20, 900);
		colorPanel.setLayout(new FlowLayout());
		colorPanel.add(new JButton("Head"));
		colorPanel.add(new JButton("Back Arm"));
		colorPanel.add(new JButton("Front Arm"));
		colorPanel.add(new JButton("Body"));
		colorPanel.add(new JButton("Back Leg"));
		colorPanel.add(new JButton("Front Leg"));
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
	
	

}