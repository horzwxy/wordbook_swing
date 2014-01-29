package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.analyzer.SentenceAnalyzer;
import me.horzwxy.app.wordbook.analyzer.WordLibrary;
import me.horzwxy.app.wordbook.analyzer.WordRecognizer;
import me.horzwxy.app.wordbook.model.AnalyzeResult;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.network.LocalProxy;
import me.horzwxy.app.wordbook.network.Proxy;
import me.horzwxy.app.wordbook.network.YinxiangProxy;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by horz on 1/21/14.
 */
public class MainFrame extends JFrame {

    private final JTextPane feedbackPane;
    private Proxy wordbookProxy;
    private SentenceAnalyzer analyzer;
    private File selectedFile;

    public MainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 300);
        setTitle("new word manager");

        JPanel controlPanel = new JPanel();
        final JTextField portInput = new JTextField(10);
        JButton portSubmit = new JButton("set port");
        portSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = Integer.parseInt(portInput.getText());
                printLog("set port " + port);
            }
        });
        JButton chooseInputFile = new JButton("choose file");
        chooseInputFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int status = fileChooser.showOpenDialog(null);
                if(status == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    printLog("choose file " + selectedFile.getAbsolutePath());
                }
            }
        });
        JButton analyse = new JButton("analyse");
        analyse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedFile == null) {
                    printLog("File not selected");
                }
                else {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                        HTMLCreator creator = new HTMLCreator();
                        if (reader.ready()) {
                            String line = reader.readLine();
                            String[] sentences = line.split("[\\.?!:]");
                            for(String sentence : sentences) {
                                List<AnalyzeResult> results = analyzer.analyzeSentence(sentence);
                                List<String> emphasizedWords = new ArrayList<String>();
                                for(AnalyzeResult result : results) {
                                    emphasizedWords.add(result.getWord().getContent());
                                }
                                creator.addSentence(sentence, emphasizedWords);
                            }
                        }
                        creator.outputDocument("1-21.html");
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        controlPanel.add(portInput);
        controlPanel.add(portSubmit);
        controlPanel.add(chooseInputFile);
        controlPanel.add(analyse);

        JPanel feedbackPanel = new JPanel();
        feedbackPane = new JTextPane();
        feedbackPane.setSize(600, 200);
        feedbackPane.setText("Ready.");
        feedbackPanel.add(feedbackPane);

        new Thread() {
            @Override
            public void run() {
                printLog("starting wordbook proxy");
                wordbookProxy = new LocalProxy();

                Map<String, Word> base = wordbookProxy.getBasicWords();
                Map<String, Word> ignored = wordbookProxy.getIgnoredWords();
                Map<String, Word> familiar = wordbookProxy.getFamiliarWords();
                Map<String, Word> unfamiliar = wordbookProxy.getUnfamiliarWords();
                Map<String, Word> unrecognized = wordbookProxy.getUnrecognizedWords();

                base.putAll(ignored);
                base.putAll(familiar);
                WordLibrary lib = new WordLibrary(base);
                WordRecognizer recognizer = new WordRecognizer(lib);
                analyzer = new SentenceAnalyzer(lib, recognizer);

                printLog("wordbook proxy started");
            }
        }.start();

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(controlPanel, BorderLayout.NORTH);
        contentPane.add(feedbackPanel, BorderLayout.SOUTH);
    }

    private void printLog(String newLine) {
        feedbackPane.setText(feedbackPane.getText() + "\n" + newLine);
    }
}
