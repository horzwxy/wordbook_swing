package me.horzwxy.app.wordbook.swing.ui;

import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.swing.controller.SwingController;

import javax.swing.*;

/**
 * Define the services it can provide to caller.
 */
public abstract class FrameModel extends JFrame {

    public static FrameModel getFrame() {
        return new DefaultFrame();
    }

    public abstract void setSwingController(SwingController controller);

    public abstract void displaySentence(Word word, String sentence);

    public abstract void displayLog(String log);
}
