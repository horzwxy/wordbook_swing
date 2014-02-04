package me.horzwxy.app.wordbook.swing.ui;

import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.swing.controller.SwingController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Default implementation of UI main frame.
 */
public class DefaultFrame extends FrameModel {

    private SwingController swingController;
    private File selectedFile;

    private final JTextPane feedbackPane;
    private final JScrollBar scrollBar;
    private final JLabel wordOfSentence;
    private final JTextArea sentenceEditor;

    DefaultFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 300);
        setTitle("new word manager");

        JPanel controlPanel = new JPanel();
        final JTextField portInput = new JTextField(10);
        portInput.setText("7962");
        JButton portSubmit = new ActionButton("start server",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new Thread() {
                            @Override
                            public void run() {
                                swingController.startServer(Integer.parseInt(portInput.getText()));
                            }
                        }.start();
                    }
                });
        JButton stopServer = new ActionButton("stop server",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        swingController.stopServer();
                    }
                });
        JButton chooseInputFile = new ActionButton("choose file",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();
                        int status = fileChooser.showOpenDialog(null);
                        if (status == JFileChooser.APPROVE_OPTION) {
                            selectedFile = fileChooser.getSelectedFile();
                            displayLog("choose file " + selectedFile.getAbsolutePath());
                        }
                    }
                });
        JButton analyse = new ActionButton("analyse", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile == null) {
                    displayLog("File not selected");
                } else {
                    swingController.analyse(selectedFile);
                }
            }
        });
        JButton displayStorage = new ActionButton("display records", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swingController.generateStorageSummary(new File("records.xml"));
            }
        });
        JButton updateStorage = new ActionButton("update records", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swingController.updateRecords();
            }
        });

        controlPanel.add(portInput);
        controlPanel.add(portSubmit);
        controlPanel.add(stopServer);
        controlPanel.add(chooseInputFile);
        controlPanel.add(analyse);
        controlPanel.add(displayStorage);
        controlPanel.add(updateStorage);

        feedbackPane = new JTextPane();
        feedbackPane.setText("Ready.\t\t");
        JScrollPane jsp = new JScrollPane(feedbackPane);
        scrollBar = jsp.getVerticalScrollBar();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new FlowLayout());
        wordOfSentence = new JLabel();
        wordOfSentence.setText("some word");
        mainPanel.add(wordOfSentence);
        sentenceEditor = new JTextArea("test");
        sentenceEditor.setLineWrap(true);
        sentenceEditor.setColumns(30);
        sentenceEditor.setRows(5);
        mainPanel.add(sentenceEditor);
        JButton sentenceSubmit = new ActionButton("update", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swingController.updateSentence(sentenceEditor.getText());
            }
        });
        mainPanel.add(sentenceSubmit);
        JButton sentenceRemove = new ActionButton("remove", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swingController.deleteSentence();
            }
        });
        mainPanel.add(sentenceRemove);

        Container contentPane = this.getContentPane();
        // BorderLayout is the default LayoutManager of JFrame's content pane
        contentPane.add(controlPanel, BorderLayout.NORTH);
        contentPane.add(jsp, BorderLayout.WEST);
        contentPane.add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void displaySentence(Word word, String sentence) {
        wordOfSentence.setText(word.getContent());
        sentenceEditor.setText(sentence);
    }

    @Override
    public void displayLog(String newLine) {
        feedbackPane.setText(feedbackPane.getText() + "\n" + newLine);
        scrollBar.setValue(scrollBar.getMaximum());
    }

    @Override
    public void setSwingController(SwingController controller) {
        this.swingController = controller;
    }

    private class ActionButton extends JButton {
        private ActionButton(String text, ActionListener listener) {
            super(text);
            addActionListener(listener);
        }
    }
}


