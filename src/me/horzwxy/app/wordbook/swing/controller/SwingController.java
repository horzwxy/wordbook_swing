package me.horzwxy.app.wordbook.swing.controller;

import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.swing.ui.FrameModel;

import java.io.File;

/**
 * Swing controller is a layer between General Controller and Swing Frame.
 */
public abstract class SwingController {

    private FrameModel frame;

    protected FrameModel getFrame() {
        return this.frame;
    }

    public void setFrame(FrameModel frame) {
        this.frame = frame;
    }

    /**
     * Start a HTTP server in a synchronized way.
     * @param port port number
     */
    public abstract void startServer(int port);

    public abstract void stopServer();

    /**
     * Do analysis work on the file, recognizing word state.
     * @param file
     */
    public abstract void analyse(File file);

    /**
     * Generate an XML file displaying all word records.
     * @param outputFile output file instance indicating output file location
     */
    public abstract void generateStorageSummary(File outputFile);

    public abstract void updateRecords();

    public abstract void displaySentence(Word word, String sentence);

    public abstract void updateSentence(String newSentence);

    /**
     * Display log on the frame.
     * @param log
     */
    public void displayLog(String log) {
        frame.displayLog(log);
    }
}
