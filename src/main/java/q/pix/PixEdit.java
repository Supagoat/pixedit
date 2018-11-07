package q.pix;

import q.pix.ui.StartupScreen;

public class PixEdit {
    public void startup() {
        new StartupScreen().setVisible(true);
    }

    public static void main(String[] args) {
        new PixEdit().startup();
    }
}
