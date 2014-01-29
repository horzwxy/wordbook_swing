package me.horzwxy.app.wordbook.swing.server;

import me.horzwxy.app.wordbook.analyzer.WordLibrary;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

/**
 * Handle adding new word request.
 */
class AddWordHandler implements RequestHandler {

    private WordLibrary wordLibrary;

    AddWordHandler(WordLibrary wordLibrary) {
        this.wordLibrary = wordLibrary;
    }

    @Override
    public void handleRequest(SimpleHttpRequest request, LocalServer.ServerCallback callback) {
        Word word = new Word(request.getParameter("word"),
                WordState.valueOf(request.getParameter("state").toUpperCase()));
        wordLibrary.addWord(word, word.getState());
        callback.onStateUpdate("add word " + word.getContent() + "/" + word.getState());
    }
}
