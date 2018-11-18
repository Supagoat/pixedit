package q.pix.ui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import q.pix.AppState;

public class DispPanel extends JFrame {

	private static final long serialVersionUID = 1L;
	JPanel panel;

	public DispPanel() {
		setSize(1000, 1000);
		setPanel(new JPanel() {
			   @Override
			    public void paintComponent(Graphics g) {
			        super.paintComponent(g);
			        Graphics2D g2 = (Graphics2D) g;
			        draw(g2);      
			    }
		});
		setLayout(new GridBagLayout());
		GridBagConstraints layoutConstraints = new GridBagConstraints();
		layoutConstraints.gridheight = 3;
		//layoutConstraints.
		//setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		add(getPanel());
		add(new Button("text"));
	}

 
    
	public void display() {
		setVisible(true);
	}
	
	public void draw(Graphics2D g2) {
		//g2.setColor(Color.red);
		//g2.fillRect(0, 0, 1000, 1000);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
		if(AppState.get().getTargetImage() != null) {
			g2.drawImage(AppState.get().getTargetImage(),0,0,null);
		}
		if(AppState.get().getInputImage() != null) {
			g2.drawImage(AppState.get().getInputImage(),0,0,null);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}




	
}