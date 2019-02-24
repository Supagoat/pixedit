package q.pix.ui.pane;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import q.pix.ui.event.ReturnToStartupListener;
import q.pix.ui.pane.paintmode.PaintingPanel;
import q.pix.util.FileUtil;
import q.pix.util.ImageUtil;

public class StartupScreen extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel panel;
	private JButton makeSetButton;
	private JButton generateButton;
	private JButton loadButton;
	private JButton quitButton;

	public StartupScreen() {
		super("Pix2pix Training Data Editor");
		setSize(600, 150);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPanel(new JPanel());
		getPanel().setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		getPanel().setLayout(new GridLayout(1, 2));
		getPanel().add(toTrainSetButton());
		getPanel().add(generateButton());
		getPanel().add(loadButton());
		getPanel().add(quitButton());
		setVisible(true);
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
		add(panel);
	}

	private JButton toTrainSetButton() {
		makeSetButton = new JButton("Trainset");
		makeSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						ImageUtil.makeTrainSet(fc.getSelectedFile().getAbsolutePath());
					} catch(Exception ex) {
						// TODO: Get alert modals done
					}
				}
			}
		});
		return makeSetButton;
	}
	
	private JButton generateButton() {
		generateButton = new JButton("Generate Inputs");
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						ImageUtil.makeGenerationInputs(fc.getSelectedFile().getAbsolutePath());
					} catch(Exception ex) {
						// TODO: Get alert modals done
					}
				}
			}
		});
		return generateButton;
	}

	private JButton loadButton() {
		loadButton = new JButton("Load");
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("Supported Files", "jpg", "png", "pxd"));
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					loadFiles(fc.getSelectedFile());
				}
			}
		});

		return loadButton;
	}

	private JButton quitButton() {
		quitButton = new JButton("Exit");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		return quitButton;
	}
	
	private void loadFiles(File selectedFile) {
		try {
			BufferedImage inputImage, targetImage;
			File input = new File(FileUtil.toInputDir(selectedFile.getAbsolutePath()));
			if (input.exists()) {
				inputImage = ImageUtil.loadAndScale(input);
			} else {
				inputImage = ImageUtil.blankImage();
			}
			File target = new File(FileUtil.toTargetDir(selectedFile.getAbsolutePath()));
			if (target.exists()) {
				targetImage = ImageUtil.loadAndScale(target);
			}	else {
				targetImage = ImageUtil.blankImage();
			}
			WorkspaceWindow dispPanel = new WorkspaceWindow();
			dispPanel.setGraphicsPanel(new PaintingPanel(dispPanel, inputImage, targetImage));
			dispPanel.setInputFilePath(dispPanel.getGraphicsPanel().getInputImage(), input.getAbsolutePath());
			dispPanel.addWindowListener(new ReturnToStartupListener(this));
			dispPanel.setBackgroundColor(targetImage);
			dispPanel.display();
			setVisible(false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	


}