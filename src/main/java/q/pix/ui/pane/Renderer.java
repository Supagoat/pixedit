package q.pix.ui.pane;

import java.util.function.Function;

public class Renderer implements Runnable {
	
	private final Function renderFunction;
	private final RenderMaster renderMaster;
	
	
	public Renderer(Function renderFunction, RenderMaster renderMaster) {
		this.renderFunction = renderFunction;
		this.renderMaster = renderMaster;
	}
	
	@Override
	public void run() {
		renderFunction.apply(null);
		renderMaster.onComplete();
	}
	
	
	
}