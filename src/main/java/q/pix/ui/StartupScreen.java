package q.pix.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import q.pix.AppState;

public class StartupScreen extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel panel;
	private JButton newButton;
	private JButton loadButton;

	public StartupScreen() {
		super("Pix2pix Training Data Editor");
		setSize(300, 150);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPanel(new JPanel());
		getPanel().setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		getPanel().setLayout(new GridLayout(1, 2));
		getPanel().add(newButton());
		getPanel().add(loadButton());
		/*
		 * try { bgImage = ImageIO.read(new File("D:\\dl\\Untitled-1.jpg")); }
		 * catch(Exception e) {} imgComponent = add(new JLabel(new ImageIcon(bgImage)),
		 * 0); imgComponent.setVisible(true); setVisible(true);
		 */
		setVisible(true);
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
		add(panel);
	}

	private JButton newButton() {
		newButton = new JButton("New");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		return newButton;
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

	private void loadFiles(File selectedFile) {
		try {
			File target = new File(selectedFile.getAbsolutePath().replace(File.separator + "target" + File.separator,
					File.separator + "input" + File.separator));
			if (target.exists()) {
				AppState.get().setInputImage(ImageIO.read(target));
			}
			File input = new File(selectedFile.getAbsolutePath().replace(File.separator + "input" + File.separator,
					File.separator + "target" + File.separator));
			if (input.exists()) {
				AppState.get().setTargetImage(ImageIO.read(input));
			}
			new DispPanel().display();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}