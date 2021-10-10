package q.pix.ui.pane;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

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
		setSize(1000, 400);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPanel(new JPanel());
		getPanel().setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		getPanel().setLayout(new GridLayout(3, 5));
		getPanel().add(toTrainSetButton());
		getPanel().add(generateButton());
		getPanel().add(loadButton());
		getPanel().add(outlineButton());
		getPanel().add(outlineDirButton());
		getPanel().add(splitButton());
		// getPanel().add(analyzeColorsButton()); // not using analyze right now
		getPanel().add(colorFamilyButton());
		getPanel().add(paintToFamilyButton());
		getPanel().add(generateFamilyTrainSets());
		getPanel().add(paintToFamilyIterationButton());
		getPanel().add(reduceColorButton());
		getPanel().add(sliceImageButton());
		getPanel().add(combineImagesButton());
		getPanel().add(sliceImagePairsButton());
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
		JButton makeSetButton = new JButton("Create p2p Trainset");
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
						makeSetButton.setText("Create p2p Trainset");
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
	
	/**
	 * Creates the inputs to the generator given a directory of input files
	 * @return The button
	 */

	private JButton generateButton() {
		JButton generateButton = new JButton("Generate p2p Generation Inputs");
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
						generateButton.setText("Generate p2p Generation Inputs");
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
		JButton loadButton = new JButton("Load For Drawing");
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
		JButton outlineButton = new JButton("Outline Image");
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
		JButton outlineDirButton = new JButton("Outline Dir");
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

	/**
	 * Creates a set of inputs and outputs for the families with input being the
	 * base family color and output being the pixels in the shaded family colors
	 * 
	 */
	private JButton paintToFamilyButton() {
		JButton paintToFamilyButton = new JButton("Paint To Family");
		paintToFamilyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser inputChooser = new JFileChooser();
					inputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = inputChooser.showOpenDialog(StartupScreen.this);

					File shadedOutputsDir = new File(inputChooser.getSelectedFile().getAbsolutePath() + "_target");
					File familyInputsDir = new File(inputChooser.getSelectedFile().getAbsolutePath() + "_input");
					if (familyInputsDir.exists() || shadedOutputsDir.exists()) {
						throw new IllegalArgumentException("Can't ovewrite a family paint dir");
					}
					shadedOutputsDir.mkdir();
					familyInputsDir.mkdir();
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File dir = FileUtil.getFamilyConfigDir(inputChooser.getSelectedFile());
						for (File imageFile : inputChooser.getSelectedFile()
								.listFiles((dirf, name) -> name.endsWith(".png"))) {
							BufferedImage image = ImageIO.read(imageFile);
							Optional<FamilyAffinity> bestConfigMatch = FileUtil
									.loadConfigFiles(ImageUtil.getImageColors(image), dir);
							ImageUtil.paintToFamily(ImageUtil.initColorGroupColors(), imageFile,
									bestConfigMatch.get().getColorFamily(), shadedOutputsDir, "");
							ImageUtil.paintInput(ImageUtil.initColorGroupColors(), imageFile,
									bestConfigMatch.get().getColorFamily(), familyInputsDir, "");
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

	public JButton reduceColorButton() {
		JButton reduceColorButton = new JButton("Reduce Colors On Waifus");
		reduceColorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser inputChooser = new JFileChooser();
					inputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = inputChooser.showOpenDialog(StartupScreen.this);

					File inputsDir = new File(inputChooser.getSelectedFile().getAbsolutePath());
					File outputsDir = new File(inputChooser.getSelectedFile().getAbsolutePath() + "_reduced");

					// outputsDir.mkdir();
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						Set<String> seenPrefixes = new HashSet<>();

						for (File prefixFinder : inputChooser.getSelectedFile()
								.listFiles((dirf, name) -> name.endsWith(".png"))) {

							String prefix = prefixFinder.getName().substring(0,
									prefixFinder.getName().indexOf("split"));
							if (!seenPrefixes.contains(prefix)) {
								File[] matching = inputChooser.getSelectedFile()
										.listFiles((dirf, name) -> name.startsWith(prefix) && name.endsWith(".png"));
								Set<Color> batchColors = new HashSet<>();
								System.out.println("Working on set " + prefix);
								for (File imageFile : matching) {
									BufferedImage image = ImageIO.read(imageFile);
									batchColors.addAll(ImageUtil.getDistinctColors(image));
								}
								System.out.println("Found " + batchColors.size() + " colors");
								// Divide them up into families automatically then select 18, ignoring green
								// background to make 19
								ImageUtil.analyzeColors(batchColors);
							}
						}
					}
				} catch (Exception ex) {
					handleError(ex);
					reduceColorButton.setText("ERROR: " + ex.toString());
				}
			}
		});

		return reduceColorButton;
	}

	/**
	 * Does paint to family but iterates over the color family combinations to
	 * eliminate the impact of choosing the "wrong" family color
	 * 
	 * And now I'm adding rotation on top
	 * 
	 * @return The button
	 */

	private JButton paintToFamilyIterationButton() {
		JButton paintToFamilyButton = new JButton("Paint To Family Iteration");
		paintToFamilyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser inputChooser = new JFileChooser();
					inputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = inputChooser.showOpenDialog(StartupScreen.this);

					File shadedOutputsDir = new File(inputChooser.getSelectedFile().getAbsolutePath() + "_target");
					File familyInputsDir = new File(inputChooser.getSelectedFile().getAbsolutePath() + "_input");
					if (familyInputsDir.exists() || shadedOutputsDir.exists()) {
						throw new IllegalArgumentException("Can't ovewrite a family paint dir");
					}
					shadedOutputsDir.mkdir();
					familyInputsDir.mkdir();
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File dir = FileUtil.getFamilyConfigDir(inputChooser.getSelectedFile());
						for (File imageFile : inputChooser.getSelectedFile()
								.listFiles((dirf, name) -> name.endsWith(".png"))) {
							BufferedImage image = ImageIO.read(imageFile);
							Optional<FamilyAffinity> bestConfigMatch = FileUtil
									.loadConfigFiles(ImageUtil.getImageColors(image), dir);
							List<List<Color>> baseColors = ImageUtil.generateColorCombos(
									ImageUtil.initColorGroupColors(), bestConfigMatch.get().getColorFamily());
							
								for (int i = 0; i < baseColors.size(); i++) {
//							ImageUtil.paintToFamilyColorIteration(baseColors.get(i), imageFile,
//									bestConfigMatch.get().getColorFamily(), shadedOutputsDir);
									ImageUtil.paintToFamilyRotation(baseColors.get(i), imageFile,
											bestConfigMatch.get().getColorFamily(), shadedOutputsDir, "f" + i);
									ImageUtil.paintInputRotation(baseColors.get(i), imageFile,
											bestConfigMatch.get().getColorFamily(), familyInputsDir, "f" + i);
								}
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

	/**
	 * 
	 * Creates BtoA trainsets of size 576x286 by iterating over every color group of
	 * a family found in each image and outputs a group-single-color to
	 * shaded-group-color training set for each group.
	 * 
	 * In other words, if an image contains 3 color groups then it will output 3
	 * training images, each input/output will be the same base
	 * color(colorGroupColors index 0, currently a red). The family base colors are
	 * basically just used as layer masks and their family colors are ignored for
	 * the purpose of this. This is based on the idea that the family colors aren't
	 * relevant and the model should learn to paint consistently regardless of which
	 * family it's painting. Inputs to generation will need to be similarly split
	 * and then re-assembled after generation.
	 * 
	 */
	private JButton generateFamilyTrainSets() {
		JButton paintToFamilyButton = new JButton("Make Ind. Fam Trainsets");
		paintToFamilyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser inputChooser = new JFileChooser();
					inputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = inputChooser.showOpenDialog(StartupScreen.this);

					File trainsetOutputDir = new File(inputChooser.getSelectedFile().getAbsolutePath() + "_trainset");

					if (trainsetOutputDir.exists()) {
						throw new IllegalArgumentException("Can't ovewrite a trainset dir");
					}
					trainsetOutputDir.mkdir();
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File dir = FileUtil.getFamilyConfigDir(inputChooser.getSelectedFile());
						for (File imageFile : inputChooser.getSelectedFile()
								.listFiles((dirf, name) -> name.endsWith(".png"))) {
							BufferedImage image = ImageIO.read(imageFile);
							Optional<FamilyAffinity> bestConfigMatch = FileUtil
									.loadConfigFiles(ImageUtil.getImageColors(image), dir);
							for (int i = 0; i < bestConfigMatch.get().getColorFamily().getColorGroups().size(); i++) {
								Set<Color> colorGroup = bestConfigMatch.get().getColorFamily().getColorGroups().get(i);
								if (!colorGroup.isEmpty()) {
									// resize to 286x286 so p2p doesn't do scaling
									ImageUtil.paintSingleGroup(i,
											ImageUtil.copyIntoCenter(image,
													ImageUtil.blankImage(286, 286, ImageUtil.GREEN_BG)),
											imageFile.getName(), trainsetOutputDir,
											bestConfigMatch.get().getColorFamily());
								}
							}
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
		JButton splitButton = new JButton("Split Sprites");
		splitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(StartupScreen.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						ImageUtil.splitSprites(fc.getSelectedFile());
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

	private JButton colorFamilyButton() {
		JButton colorFamilyButton = new JButton("Assign Color Families");
		colorFamilyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);
				File imageFile = null;
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					imageFile = fc.getSelectedFile();
				}
				beginColorFamilyEdit(imageFile, FileUtil.getFamilyConfigDir(imageFile));

			}
		});

		return colorFamilyButton;
	}

	private JButton sliceImageButton() {
		JButton generateButton = new JButton("Slice Files");
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File inputDir = fc.getSelectedFile();
					fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					returnVal = fc.showOpenDialog(StartupScreen.this);
					File outputDir = fc.getSelectedFile();
					try {
						generateButton.setText("Slicing....");
						ImageUtil.sliceImages(inputDir, outputDir);
						generateButton.setText("SliceImage");
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

	private JButton sliceImagePairsButton() {
		JButton generateButton = new JButton("Slice Image Pairs");
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File parentDir = fc.getSelectedFile();
					try {
						generateButton.setText("Slicing....");
						ImageUtil.sliceImagePairs(parentDir);
						generateButton.setText("Slice Image Pairs");
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

	private JButton combineImagesButton() {
		JButton generateButton = new JButton("Recombine Files");
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(StartupScreen.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File inputDir = fc.getSelectedFile();
					fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					returnVal = fc.showOpenDialog(StartupScreen.this);
					File outputDir = fc.getSelectedFile();
					try {
						generateButton.setText("Combining....");
						ImageUtil.combineImages(inputDir, outputDir);
						generateButton.setText("Recombine Files");
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
			dispPanel.makeSaveButton(queue::getCurrentFamily, savedFamiliesDir.getAbsolutePath(),
					queue::getInputFileName, queue::queueStep);

			dispPanel.addWindowListener(new ReturnToStartupListener(this));

			setVisible(false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}