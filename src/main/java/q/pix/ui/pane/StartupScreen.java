package q.pix.ui.pane;

import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import q.pix.colorfamily.ColorFamily;
import q.pix.colorfamily.FamilyAffinity;
import q.pix.colorfamily.PaintingQueue;
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

	public StartupScreen() {
		super("Pix2pix Training Data Editor");
		setSize(1000, 300);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPanel(new JPanel());
		getPanel().setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		getPanel().setLayout(new GridLayout(2, 2));
		getPanel().add(toTrainSetButton());
		getPanel().add(generateButton());
		getPanel().add(loadButton());
		getPanel().add(outlineButton());
		getPanel().add(outlineDirButton());
		getPanel().add(splitButton());
		// getPanel().add(analyzeColorsButton()); // not using analyze right now
		getPanel().add(colorFamilyButton());
		getPanel().add(paintToFamilyButton());

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
		JButton makeSetButton = new JButton("Trainset");
		makeSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						makeSetButton.setText("Generating....");
						ImageUtil.makeTrainSet(fc.getSelectedFile().getAbsolutePath());
						makeSetButton.setText("Trainset");
					} catch (Exception ex) {
						// TODO: Get alert modals done
						handleError(ex);
						// makeSetButton.setText("ERROR: " + ex.toString());
					}
				}
			}
		});
		return makeSetButton;
	}

	private JButton generateButton() {
		JButton generateButton = new JButton("Generate Inputs");
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						generateButton.setText("Generating....");
						ImageUtil.makeGenerationInputs(fc.getSelectedFile().getAbsolutePath());
						generateButton.setText("Generate Inputs");
					} catch (Exception ex) {
						// TODO: Get alert modals done
						// generateButton.setText("ERROR: " + ex.toString());
						handleError(ex);
					}
				}
			}
		});
		return generateButton;
	}

	private JButton loadButton() {
		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("Supported Files", "jpg", "png", "pxd"));
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					loadFileForSideBySideEdit(fc.getSelectedFile());
				}
			}
		});

		return loadButton;
	}

	private JButton outlineButton() {
		JButton outlineButton = new JButton("Outline");
		outlineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileNameExtensionFilter("Supported Files", "jpg", "png", "pxd"));
					int returnVal = fc.showOpenDialog(StartupScreen.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						ImageUtil.outlineFile(fc.getSelectedFile());
					}
				} catch (Exception ex) {
					handleError(ex);
					outlineButton.setText("ERROR: " + ex.toString());
				}
			}
		});

		return outlineButton;
	}

	private JButton outlineDirButton() {
		JButton outlineDirButton = new JButton("OutlineDir");
		outlineDirButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(StartupScreen.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						ImageUtil.outlineFile(fc.getSelectedFile());
					}
				} catch (Exception ex) {
					handleError(ex);
					outlineDirButton.setText("ERROR: " + ex.toString());
				}
			}
		});
		return outlineDirButton;
	}

	private JButton paintToFamilyButton() {
		JButton paintToFamilyButton = new JButton("Paint To Family");
		paintToFamilyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(StartupScreen.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File dir = FileUtil.getFamilyConfigDir(fc.getSelectedFile());
						for (File imageFile : fc.getSelectedFile().listFiles((dirf, name) -> name.endsWith(".png"))) {
							BufferedImage image = ImageIO.read(imageFile);
							Optional<FamilyAffinity> bestConfigMatch = FileUtil
									.loadConfigFiles(ImageUtil.getImageColors(image), dir);
							ImageUtil.paintToFamily(ImageUtil.initColorGroupColors(), imageFile,
									bestConfigMatch.get().getColorFamily());
						}
					}
				} catch (Exception ex) {
					handleError(ex);
					paintToFamilyButton.setText("ERROR: " + ex.toString());
				}
			}
		});

		return paintToFamilyButton;
	}

	private JButton splitButton() {
		JButton splitButton = new JButton("Split");
		splitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(StartupScreen.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						ImageUtil.splitImages(fc.getSelectedFile());
						splitButton.setText("DONE");
					}
				} catch (Exception ex) {
					handleError(ex);
					splitButton.setText("ERROR: " + ex.toString());
				}
			}
		});

		return splitButton;
	}

	private JButton analyzeColorsButton() {
		JButton analyzeColorsButton = new JButton("Analyze Colors");
		analyzeColorsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(StartupScreen.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						ImageUtil.analyzeColors(fc.getSelectedFile());
					}
				} catch (Exception ex) {
					handleError(ex);
					analyzeColorsButton.setText("ERROR: " + ex.toString());
				}
			}
		});

		return analyzeColorsButton;
	}

//	private JButton paintFromFamiliesButton() {
//		JButton paintFromFamiliesButton = new JButton("Paint From Fam");
//		paintFromFamiliesButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				try {
//					JFileChooser fc = new JFileChooser();
//					fc.setFileFilter(new FileNameExtensionFilter("Supported Files", "jpg", "png", "pxd"));
//					int returnVal = fc.showOpenDialog(StartupScreen.this);
//
//					if (returnVal == JFileChooser.APPROVE_OPTION) {
//						ImageUtil.analyzeColors(fc.getSelectedFile());
//					}
//				} catch (Exception ex) {
//					handleError(ex);
//					paintFromFamiliesButton.setText("ERROR: " + ex.toString());
//				}
//			}
//		});
//
//		return paintFromFamiliesButton;
//	}

	private JButton colorFamilyButton() {
		JButton colorFamilyButton = new JButton("Color Family");
		colorFamilyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				//fc.setFileFilter(new FileNameExtensionFilter("Supported Files", "jpg", "png", "pxd"));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);
				File imageFile = null;
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					imageFile = fc.getSelectedFile();
				}

//				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//				returnVal = fc.showOpenDialog(StartupScreen.this);
//				File familiesConfigDir = null;
//
//				if (returnVal == JFileChooser.APPROVE_OPTION) {
//					familiesConfigDir = fc.getSelectedFile();
//				}

				beginColorFamilyEdit(imageFile, FileUtil.getFamilyConfigDir(imageFile));

			}
		});

		return colorFamilyButton;
	}

	private void handleError(Exception e) {
		e.printStackTrace();
		getPanel().add(new TextArea(e.toString()));
	}

	private JButton quitButton() {
		JButton quitButton = new JButton("Exit");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		return quitButton;
	}

	private void loadFileForSideBySideEdit(File selectedFile) {
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
			} else {
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

	private void beginColorFamilyEdit(File selectedDir, File savedFamiliesDir) {
		try {
			PaintingQueue queue = new PaintingQueue();
			ColorFamilyWindow dispPanel = new ColorFamilyWindow();
			dispPanel.setOnSaveCallback(queue::queueStep);
			dispPanel.display();
			queue.setup(selectedDir, savedFamiliesDir, dispPanel::setNewImage);
			dispPanel.makeSaveButton(queue::getCurrentFamily, savedFamiliesDir.getAbsolutePath(), queue::getInputFileName, queue::queueStep);
			//dispPanel.setGraphicsPanel(new ColorFamilyPickerDisplay(dispPanel, dispPanel.));
			
			dispPanel.addWindowListener(new ReturnToStartupListener(this));

			setVisible(false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}