package q.pix.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class WorkspaceWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel graphicsPanel;
	private DisplayMode displayMode;
	private BufferedImage inputImage;
	private BufferedImage targetImage;
	public static final int IMAGE_SIZE=256; 
	
	public enum DisplayMode {
		Overlay, SideBySide
	}
	
	public WorkspaceWindow() {
		setSize(getZoomLevel()*AppState.IMAGE_SIZE+getHorizontalUISize(), getZoomLevel()*AppState.IMAGE_SIZE+getVerticalUISize());
		setGraphicsPanel(buildDisplayPanel());
		setLayout(new BorderLayout());

		add(getGraphicsPanel(), BorderLayout.CENTER);
		JPanel topPanel = new JPanel();
		topPanel.setSize(1000, 20);
		topPanel.setLayout(new FlowLayout());
		topPanel.add(new Button("Split"));
		topPanel.add(new Button("Overlay"));
		topPanel.get
		add(topPanel, BorderLayout.PAGE_START);
	}

	public void display() {
		setVisible(true);
	}

	private JPanel buildDisplayPanel() {
		return new GraphicsPanel();
	}

	private JButton buildDispModeOverlay() {
		JButton button = new JButton("Overlay");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
	}

	public JPanel getGraphicsPanel() {
		return graphicsPanel;
	}

	public WorkspaceWindow setGraphicsPanel(JPanel graphicsPanel) {
		this.graphicsPanel = graphicsPanel;
		return this;
	}
	
	public BufferedImage getInputImage() {
		return inputImage;
	}

	public WorkspaceWindow setInputImage(BufferedImage inputImage) {
		this.inputImage = inputImage;
		return this;
	}

	public BufferedImage getTargetImage() {
		return targetImage;
	}

	public WorkspaceWindow setTargetImage(BufferedImage targetImage) {
		this.targetImage = targetImage;
		return this;
	}

}