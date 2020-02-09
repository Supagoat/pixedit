package q.pix.ui.pane;

import java.util.Optional;

import q.pix.ui.pane.WorkspaceWindow.DisplayMode;

public interface WorkspacePaintWindow {
	void setBackgroundColor(int color);
	Optional<Integer> getBackgroundColor();
	DisplayMode getDisplayMode();
}
