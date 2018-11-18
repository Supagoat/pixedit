//package q.pix;
//
//import java.awt.image.BufferedImage;
//
//public class AppState {
//	private BufferedImage inputImage;
//	private BufferedImage targetImage;
//	public static final int IMAGE_SIZE=256; 
//	
//	public static AppState get() {
//		return Singleton.INSTANCE.get();
//	}
//	
//	public BufferedImage getInputImage() {
//		return inputImage;
//	}
//
//	public AppState setInputImage(BufferedImage inputImage) {
//		this.inputImage = inputImage;
//		return this;
//	}
//
//	public BufferedImage getTargetImage() {
//		return targetImage;
//	}
//
//	public AppState setTargetImage(BufferedImage targetImage) {
//		this.targetImage = targetImage;
//		return this;
//	}
//
//
//
//
//	public enum Singleton {
//	    INSTANCE;
//		
//		AppState appState = new AppState();
//		
//		public AppState get() {
//			return appState;
//		}
//	}
//	
//	
//	
//}