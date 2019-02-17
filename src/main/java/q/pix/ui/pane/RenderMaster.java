package q.pix.ui.pane;

import java.util.concurrent.ThreadPoolExecutor;

public class RenderMaster {
	
	private int expectedWorkUnits;
	private volatile int completedWorkUnits;
	private ThreadPoolExecutor executor;
	
	public RenderMaster (ThreadPoolExecutor executor, int expectedWorkUnits) {
		setExecutor(executor);
		setExpectedWorkUnits(expectedWorkUnits);
	}
	
	public void onComplete() {
		synchronized(this) {
			setCompletedWorkUnits(getCompletedWorkUnits()+1);
			if(getExpectedWorkUnits() == getCompletedWorkUnits()) {
				notifyAll();
			}
		}
	}
	
	public void submit(Runnable f) {
		getExecutor().execute(new Runner(f));
	}

	public int getCompletedWorkUnits() {
		return completedWorkUnits;
	}

	public RenderMaster setCompletedWorkUnits(int completedWorkUnits) {
		this.completedWorkUnits = completedWorkUnits;
		return this;
	}

	public int getExpectedWorkUnits() {
		return expectedWorkUnits;
	}

	public RenderMaster setExpectedWorkUnits(int expectedWorkUnits) {
		this.expectedWorkUnits = expectedWorkUnits;
		return this;
	}
	
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	public RenderMaster setExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
		return this;
	}

	private class Runner implements Runnable {
		private final Runnable function;
		public Runner(Runnable toRun) {
			this.function = toRun;
		}
		public void run() {
			function.run();
			RenderMaster.this.onComplete();
		}
	}

	
}