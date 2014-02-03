package me.horzwxy.app.wordbook.swing.controller;

import me.horzwxy.app.wordbook.swing.ui.FrameModel;

/**
 * App launcher.
 */
public class Launcher {

    public static void main(String[] args) {
        ControllerModel cm = ControllerModel.getController();
        // frame and its controller
        FrameModel fm = FrameModel.getFrame();
        SwingController sc = cm.getSwingController();
        sc.setFrame(fm);
        fm.setSwingController(sc);
        // start frame
        fm.setVisible(true);
    }
}
