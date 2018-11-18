package q.pix.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ReturnToStartupListener implements WindowListener {
	private StartupScreen startupScreen;
	
	public ReturnToStartupListener(StartupScreen startup) {
		setStartupScreen(startup);
	}
	
	
	
	@Override
	public void windowClosed(WindowEvent arg0) {

	}



	@Override
	public void windowClosing(WindowEvent arg0) {
		getStartupScreen().setVisible(true);
		
	}

	
	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}



	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}



	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

	public StartupScreen getStartupScreen() {
		return startupScreen;
	}

	public ReturnToStartupListener setStartupScreen(StartupScreen startupScreen) {
		this.startupScreen = startupScreen;
		return this;
	}

	
}