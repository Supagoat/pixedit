package q.pix.ui.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import q.pix.ui.pane.WorkspaceWindow;
import q.pix.ui.pane.WorkspaceWindow.DisplayMode;

public class DisplayModeButton extends JButton {
	private static final long serialVersionUID = 1L;

	public DisplayModeButton(WorkspaceWindow workspace) {
		setText(getToggledDisplayMode(workspace.getDisplayMode()).toString());
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workspace.setDisplayMode(getToggledDisplayMode(workspace.getDisplayMode()));
				setText(getToggledDisplayMode(workspace.getDisplayMode()).toString());
				workspace.getGraphicsPanel().repaint();
			}
		});
	}
	
	private WorkspaceWindow.DisplayMode getToggledDisplayMode(DisplayMode displayMode) {
		return displayMode == DisplayMode.Overlay ? DisplayMode.SideBySide : DisplayMode.Overlay;
	}
	
}