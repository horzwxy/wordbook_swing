package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.analyzer.SentenceAnalyzer;
import me.horzwxy.app.wordbook.analyzer.WordLibrary;
import me.horzwxy.app.wordbook.analyzer.WordRecognizer;
import me.horzwxy.app.wordbook.model.AnalyzeResult;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;
import me.horzwxy.app.wordbook.network.LocalProxy;
import me.horzwxy.app.wordbook.network.Proxy;
import me.horzwxy.app.wordbook.network.YinxiangProxy;
import me.horzwxy.app.wordbook.swing.server.LocalServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Main frame.
 */
public class MainFrame extends JFrame {

    private final JTextPane feedbackPane;
    private Proxy wordbookProxy;
    private SentenceAnalyzer analyzer;
    private File selectedFile;
    private LocalServer server;
    private int port;
    private WordLibrary wordLibrary;
    private final JScrollBar scrollBar;

    public MainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 300);
        setTitle("new word manager");

        JPanel controlPanel = new JPanel();
        final JTextField portInput = new JTextField(10);
        portInput.setText("7962");
        JButton portSubmit = new JButton("start server");
        portSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                port = Integer.parseInt(portInput.getText());
                try {
                    server = new LocalServer(port, wordLibrary, new LocalServer.ServerCallback() {
                        @Override
                        public void onStateUpdate(String newState) {
                            printLog(newState);
                        }
                    });
                    server.start();
                    printLog("server started");
                } catch (IOException e1) {
                    e1.printStackTrace();
                    printLog("fail to start server");
                }
            }
        });
        JButton stopServer = new JButton("stop server");
        stopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    server.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
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
                        HTMLCreator creator = new HTMLCreator(port);
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
        JButton updateStorage = new JButton("update records");
        updateStorage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wordbookProxy.updateWords(wordLibrary);
                printLog("update word storage");
            }
        });
        controlPanel.add(portInput);
        controlPanel.add(portSubmit);
        controlPanel.add(stopServer);
        controlPanel.add(chooseInputFile);
        controlPanel.add(analyse);
        controlPanel.add(updateStorage);

        feedbackPane = new JTextPane();
        feedbackPane.setSize(600, 200);
        feedbackPane.setText("Ready.");
        JScrollPane jsp = new JScrollPane(feedbackPane);
        scrollBar = jsp.getVerticalScrollBar();

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

                wordLibrary = new WordLibrary();
                wordLibrary.addWords(base.values(), WordState.BASIC);
                wordLibrary.addWords(ignored.values(), WordState.IGNORED);
                wordLibrary.addWords(familiar.values(), WordState.FAMILIAR);
                wordLibrary.addWords(unfamiliar.values(), WordState.UNFAMILIAR);
                wordLibrary.addWords(unrecognized.values(), WordState.UNFAMILIAR);

                WordRecognizer recognizer = new WordRecognizer(wordLibrary);
                analyzer = new SentenceAnalyzer(wordLibrary, recognizer);

                printLog("wordbook proxy started");
            }
        }.start();

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(controlPanel, BorderLayout.NORTH);
        contentPane.add(jsp, BorderLayout.WEST);
    }

    private void printLog(String newLine) {
        feedbackPane.setText(feedbackPane.getText() + "\n" + newLine);
        scrollBar.setValue(scrollBar.getMaximum());
    }
}
