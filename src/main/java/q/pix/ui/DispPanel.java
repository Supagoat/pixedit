package q.pix.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

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
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		add(getPanel());
	}

 
    
	public void display() {
		setVisible(true);
	}
	
	public void draw(Graphics2D g2) {
		g2.setColor(Color.red);
		g2.fillRect(0, 0, 1000, 1000);
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