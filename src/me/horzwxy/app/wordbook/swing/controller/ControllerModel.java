package me.horzwxy.app.wordbook.swing.controller;

import com.sun.corba.se.spi.orbutil.fsm.Input;
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
import me.horzwxy.app.wordbook.network.YinxiangProxy;
import me.horzwxy.app.wordbook.swing.AnalyseResultHTMLCreator;
import me.horzwxy.app.wordbook.swing.XMLCreator;

import java.io.*;
import java.net.InetSocketAddress;
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
    private UpdateSentenceData updateSentenceData;

    private final SwingController swingController = new SwingController() {

        @Override
        public void displaySentence(Word word, String sentence) {
            getFrame().displaySentence(word, sentence);
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
        public void updateSentence(String newSentence) {
            DefaultController.this.updateSentence(newSentence);
        }


    };
    private final HttpHandler addWordHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Map<String, String> attrs = Tool.parseParameters(httpExchange.getRequestURI().getQuery());
            WordState state = WordState.valueOf(attrs.get("state").toUpperCase());
            DefaultController.this.addWord(attrs.get("word"), state);

            InputStream is = httpExchange.getRequestBody();
            while(is.read() > 0) {
                // nothing, keep reading
            }
            is.close();

            String response = "This is the response";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    };
    private final HttpHandler updateSentenceHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Map<String, String> attrs = Tool.parseParameters(httpExchange.getRequestURI().getQuery());
            String wordContent = attrs.get("word");
            int sentenceHash = Integer.parseInt(attrs.get("hash"));
            DefaultController.this.displaySentence(wordContent, sentenceHash);

            InputStream is = httpExchange.getRequestBody();
            while(is.read() > 0) {
                // nothing, keep reading
            }
            is.close();

            String response = "This is the response";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
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
            swingController.displayLog("server started.");
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

    private void addWord(String wordContent, WordState state) {
        Word word = wordLibrary.getWord(wordContent.toLowerCase());
        WordState originalState = word.getState();
        word.setState(state);
        wordLibrary.updateWord(word, originalState);

        swingController.displayLog("add word " + word.getContent() + "/" + word.getState());
    }

    private void analyse(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            AnalyseResultHTMLCreator creator = new AnalyseResultHTMLCreator(localServer.getAddress().getPort());
            String line;
            while ((line = reader.readLine()) != null) {
                String[] sentences = line.split("[\\.?!:]");
                for(String sentence : sentences) {
                    sentence += ".";
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
        wordbookProxy = new YinxiangProxy();

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
    }

    private void displaySentence(String wordContent, int sentenceHash) {
        Word word = wordLibrary.getWord(wordContent.toLowerCase());
        for(String sentence : word.getSentences()) {
            if(sentence.hashCode() == sentenceHash) {
                updateSentenceData = new UpdateSentenceData(word, sentence.hashCode());
                swingController.displaySentence(word, sentence);
                break;
            }
        }
    }

    private void updateSentence(String newSentence) {
        Word word = updateSentenceData.getWord();
        for(int i = 0; i < word.getSentences().size(); i++) {
            String sentence = word.getSentences().get(i);
            if(sentence.hashCode() == updateSentenceData.getOriginalSentenceHash()) {
                word.getSentences().remove(i);
                word.getSentences().add(newSentence);
                swingController.displayLog("update sentence of " + word.getContent());
                return;
            }
        }
    }

    private static class UpdateSentenceData {
        private Word word;
        private int originalSentenceHash;

        UpdateSentenceData(Word word, int originalSentenceHash) {
            this.word = word;
            this.originalSentenceHash = originalSentenceHash;
        }

        public Word getWord() {
            return word;
        }

        public int getOriginalSentenceHash() {
            return originalSentenceHash;
        }
    }
}