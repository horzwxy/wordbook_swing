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
        Word word = wordLibrary.getWord(request.getParameter("word").toLowerCase());
        WordState originalState = word.getState();
        word.setState(WordState.valueOf(request.getParameter("state").toUpperCase()));
        wordLibrary.updateWord(word, originalState);

        callback.onStateUpdate("add word " + word.getContent() + "/" + word.getState());
    }
}
