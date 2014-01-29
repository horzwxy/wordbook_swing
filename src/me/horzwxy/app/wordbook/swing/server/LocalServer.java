package me.horzwxy.app.wordbook.swing.server;

import me.horzwxy.app.wordbook.analyzer.WordLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple server handles all request from Web browser.
 */
public class LocalServer extends ServerSocket {

    private Thread serverThread;
    private Map<String, RequestHandler> handlerMap;
    private ServerCallback callback;

    public LocalServer(int port, WordLibrary wordLibrary, ServerCallback callback) throws IOException {
        super(port);

        handlerMap = new HashMap<String, RequestHandler>();
        handlerMap.put("addnewword", new AddWordHandler(wordLibrary));
        this.callback = callback;
    }

    public void start() {
        serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        Socket client = accept();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        SimpleHttpRequest request = new SimpleHttpRequest(reader.readLine());
                        handlerMap.get(request.getPattern()).handleRequest(request, callback);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        serverThread.start();
    }

    public void stop() {
        serverThread.interrupt();
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ServerCallback {
        public void onStateUpdate(String newState);
    }
}
