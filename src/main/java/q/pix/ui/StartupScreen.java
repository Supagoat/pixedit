package q.pix.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import q.pix.AppState;
import q.pix.util.ImageUtil;

public class StartupScreen extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel panel;
	private JButton newButton;
	private JButton loadButton;
	private JButton quitButton;

	public StartupScreen() {
		super("Pix2pix Training Data Editor");
		setSize(300, 150);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPanel(new JPanel());
		getPanel().setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		getPanel().setLayout(new GridLayout(1, 2));
		getPanel().add(newButton());
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
			File input = new File(selectedFile.getAbsolutePath().replace(File.separator + "target" + File.separator,
					File.separator + "input" + File.separator));
			if (input.exists()) {
				AppState.get().setInputImage(ImageUtil.loadAndScale(input));
			}
			File target = new File(selectedFile.getAbsolutePath().replace(File.separator + "input" + File.separator,
					File.separator + "target" + File.separator));
			if (target.exists()) {
				AppState.get().setTargetImage(ImageUtil.loadAndScale(target));
			}
			DispPanel dispPanel = new DispPanel();
			dispPanel.addWindowListener(new ReturnToStartupListener(this));
			dispPanel.display();
			setVisible(false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

}