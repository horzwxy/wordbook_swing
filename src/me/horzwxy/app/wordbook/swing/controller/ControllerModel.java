package me.horzwxy.app.wordbook.swing.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.horzwxy.app.wordbook.analyzer.SentenceAnalyzer;
import me.horzwxy.app.wordbook.analyzer.WordLibrary;
import me.horzwxy.app.wordbook.analyzer.WordRecognizer;
import me.horzwxy.app.wordbook.model.AnalyzeResult;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;
import me.horzwxy.app.wordbook.network.LocalProxy;
import me.horzwxy.app.wordbook.network.Proxy;
import me.horzwxy.app.wordbook.swing.AnalyseResultHTMLCreator;
import me.horzwxy.app.wordbook.swing.XMLCreator;

import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;
import java.util.List;

/**
 * Define the capabilities that the background model can do.
 */
public abstract class ControllerModel {

    public static ControllerModel getController() {
        return new DefaultController();
    }

    public abstract SwingController getSwingController();

    public abstract HttpHandler getAddWordHandler();

    public abstract HttpHandler getUpdateSentenceHandler();
}

class DefaultController extends ControllerModel {

    private HttpServer localServer;
    private Proxy wordbookProxy;
    private WordLibrary wordLibrary;
    private SentenceAnalyzer analyzer;

    private final SwingController swingController = new SwingController() {
        @Override
        public void displaySentence(Word word, String sentence) {

        }

        @Override
        public void startServer(int port) {
            DefaultController.this.startServer(port);
        }

        @Override
        public void stopServer() {
            DefaultController.this.stopServer();
        }

        @Override
        public void analyse(File file) {
            DefaultController.this.analyse(file);
        }

        @Override
        public void generateStorageSummary(File outputFile) {
            DefaultController.this.generateStorageSummary(outputFile);
        }

        @Override
        public void updateRecords() {
            DefaultController.this.updateRecords();
        }

        @Override
        public void updateSentence(Word word, String originalSentence, String newSentence) {

        }
    };
    private final HttpHandler addWordHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

        }
    };
    private final HttpHandler updateSentenceHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

        }
    };

    DefaultController() {
        new Thread() {
            @Override
            public void run() {
                initProxy();
            }
        }.start();
    }

    @Override
    public SwingController getSwingController() {
        return swingController;
    }

    @Override
    public HttpHandler getAddWordHandler() {
        return addWordHandler;
    }

    @Override
    public HttpHandler getUpdateSentenceHandler() {
        return updateSentenceHandler;
    }

    private void startServer(int port) {
        try {
            localServer = HttpServer.create(new InetSocketAddress(port), 0);
            localServer.createContext("/addnewword", addWordHandler);
            localServer.createContext("/updatesentence", updateSentenceHandler);
            localServer.setExecutor(null); // creates a default executor
            localServer.start();
        } catch (IOException e1) {
            e1.printStackTrace();
            swingController.displayLog("fail to start server: " + e1.getMessage());
        }
    }

    private void stopServer() {
        if (localServer != null) {
            // no waiting-time for unhandled HTTP request
            localServer.stop(0);
        }
        swingController.displayLog("server has stopped");
    }

    private void addWord() {

    }

    private void analyse(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            AnalyseResultHTMLCreator creator = new AnalyseResultHTMLCreator(localServer.getAddress().getPort());
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

            Tool.browse(outputFile.toURI());

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void generateStorageSummary(File outputFile) {
        Map<WordState, Collection<Word>> wordListMap = new HashMap<WordState, Collection<Word>>();
        for(WordState state : WordState.values()) {
            if(state == WordState.BASIC) {
                continue;
            }
            wordListMap.put(state, wordLibrary.getWords(state));
        }
        Map<String, String> attrMap = new HashMap<String, String>();
        attrMap.put("port", localServer.getAddress().getPort() + "");
        XMLCreator.generateWordList(wordListMap, attrMap, outputFile);

        Tool.browse(outputFile.toURI());
    }

    private void updateRecords() {
        wordbookProxy.updateWords(wordLibrary);
        swingController.displayLog("update word storage");
    }

    private void initProxy() {
        swingController.displayLog("starting wordbook proxy");

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

        swingController.displayLog("wordbook proxy started");
    }

    private void updateSentence(Word word, String originalSentence, String newSentence) {

    }
}