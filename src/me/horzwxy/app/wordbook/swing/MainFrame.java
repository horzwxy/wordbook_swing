package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.analyzer.SentenceAnalyzer;
import me.horzwxy.app.wordbook.analyzer.WordLibrary;
import me.horzwxy.app.wordbook.analyzer.WordRecognizer;
import me.horzwxy.app.wordbook.model.AnalyzeResult;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;
import me.horzwxy.app.wordbook.network.LocalProxy;
import me.horzwxy.app.wordbook.network.Proxy;
import me.horzwxy.app.wordbook.swing.server.LocalServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
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
    private JPanel mainPanel;

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

                        @Override
                        public void onNewWordAdded(Word word) {
                            mainPanel.removeAll();
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
                        AnalyseResultHTMLCreator creator = new AnalyseResultHTMLCreator(port);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] sentences = line.split("[\\.?!:]");
                            for(String sentence : sentences) {
                                List<AnalyzeResult> results = analyzer.analyzeSentence(sentence);
                                List<Word> emphasizedWords = new ArrayList<Word>();
                                for(AnalyzeResult result : results) {
                                    emphasizedWords.add(result.getWord());
                                }
                                creator.addSentence(sentence, emphasizedWords);
                            }
                        }

                        File outputFile = new File("1-21.html");
                        creator.outputDocument(outputFile);

                        DesktopApi.browse(outputFile.toURI());

                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        JButton displayStorage = new JButton("display records");
        displayStorage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                printRecords();
                SentenceEditorHTMLCreator creator = new SentenceEditorHTMLCreator(port, wordLibrary);
                File outputFile = new File("records.html");
                creator.outputDocument(outputFile);

                DesktopApi.browse(outputFile.toURI());
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
        controlPanel.add(displayStorage);
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

        mainPanel = new JPanel();
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(controlPanel, BorderLayout.NORTH);
        contentPane.add(jsp, BorderLayout.WEST);
        contentPane.add(mainScrollPane, BorderLayout.CENTER);
    }

    private void printLog(String newLine) {
        feedbackPane.setText(feedbackPane.getText() + "\n" + newLine);
        scrollBar.setValue(scrollBar.getMaximum());
    }

    private void printRecords() {
        wordLibrary.printRecords();
    }
}

class DesktopApi {

    public static boolean browse(URI uri) {

        if (openSystemSpecific(uri.toString())) return true;

        if (browseDESKTOP(uri)) return true;

        return false;
    }


    public static boolean open(File file) {

        if (openSystemSpecific(file.getPath())) return true;

        if (openDESKTOP(file)) return true;

        return false;
    }


    public static boolean edit(File file) {

        // you can try something like
        // runCommand("gimp", "%s", file.getPath())
        // based on user preferences.

        if (openSystemSpecific(file.getPath())) return true;

        if (editDESKTOP(file)) return true;

        return false;
    }


    private static boolean openSystemSpecific(String what) {

        EnumOS os = getOs();

        if (os.isLinux()) {
            if (runCommand("kde-open", "%s", what)) return true;
            if (runCommand("gnome-open", "%s", what)) return true;
            if (runCommand("xdg-open", "%s", what)) return true;
        }

        if (os.isMac()) {
            if (runCommand("open", "%s", what)) return true;
        }

        if (os.isWindows()) {
            if (runCommand("explorer", "%s", what)) return true;
        }

        return false;
    }


    private static boolean browseDESKTOP(URI uri) {

        logOut("Trying to use Desktop.getDesktop().browse() with " + uri.toString());
        try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.");
                return false;
            }

            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                logErr("BORWSE is not supported.");
                return false;
            }

            Desktop.getDesktop().browse(uri);

            return true;
        } catch (Throwable t) {
            logErr("Error using desktop browse.", t);
            return false;
        }
    }


    private static boolean openDESKTOP(File file) {

        logOut("Trying to use Desktop.getDesktop().open() with " + file.toString());
        try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.");
                return false;
            }

            if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                logErr("OPEN is not supported.");
                return false;
            }

            Desktop.getDesktop().open(file);

            return true;
        } catch (Throwable t) {
            logErr("Error using desktop open.", t);
            return false;
        }
    }


    private static boolean editDESKTOP(File file) {

        logOut("Trying to use Desktop.getDesktop().edit() with " + file);
        try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.");
                return false;
            }

            if (!Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
                logErr("EDIT is not supported.");
                return false;
            }

            Desktop.getDesktop().edit(file);

            return true;
        } catch (Throwable t) {
            logErr("Error using desktop edit.", t);
            return false;
        }
    }


    private static boolean runCommand(String command, String args, String file) {

        logOut("Trying to exec:\n   cmd = " + command + "\n   args = " + args + "\n   %s = " + file);

        String[] parts = prepareCommand(command, args, file);

        try {
            Process p = Runtime.getRuntime().exec(parts);
            if (p == null) return false;

            try {
                int retval = p.exitValue();
                if (retval == 0) {
                    logErr("Process ended immediately.");
                    return false;
                } else {
                    logErr("Process crashed.");
                    return false;
                }
            } catch (IllegalThreadStateException itse) {
                logErr("Process is running.");
                return true;
            }
        } catch (IOException e) {
            logErr("Error running command.", e);
            return false;
        }
    }


    private static String[] prepareCommand(String command, String args, String file) {

        List<String> parts = new ArrayList<String>();
        parts.add(command);

        if (args != null) {
            for (String s : args.split(" ")) {
                s = String.format(s, file); // put in the filename thing

                parts.add(s.trim());
            }
        }

        return parts.toArray(new String[parts.size()]);
    }

    private static void logErr(String msg, Throwable t) {
//        System.err.println(msg);
//        t.printStackTrace();
    }

    private static void logErr(String msg) {
//        System.err.println(msg);
    }

    private static void logOut(String msg) {
//        System.out.println(msg);
    }

    public static enum EnumOS {
        linux, macos, solaris, unknown, windows;

        public boolean isLinux() {

            return this == linux || this == solaris;
        }


        public boolean isMac() {

            return this == macos;
        }


        public boolean isWindows() {

            return this == windows;
        }
    }


    public static EnumOS getOs() {

        String s = System.getProperty("os.name").toLowerCase();

        if (s.contains("win")) {
            return EnumOS.windows;
        }

        if (s.contains("mac")) {
            return EnumOS.macos;
        }

        if (s.contains("solaris")) {
            return EnumOS.solaris;
        }

        if (s.contains("sunos")) {
            return EnumOS.solaris;
        }

        if (s.contains("linux")) {
            return EnumOS.linux;
        }

        if (s.contains("unix")) {
            return EnumOS.linux;
        } else {
            return EnumOS.unknown;
        }
    }
}
