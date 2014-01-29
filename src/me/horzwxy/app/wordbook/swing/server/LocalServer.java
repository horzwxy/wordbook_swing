package me.horzwxy.app.wordbook.swing.server;

import me.horzwxy.app.wordbook.analyzer.WordLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private int port;

    public LocalServer(int port, WordLibrary wordLibrary, ServerCallback callback) throws IOException {
        super(port);

        this.port = port;

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

                        PrintWriter writer = new PrintWriter(client.getOutputStream());
                        writer.println("HTTP/1.1 200 OK");
                        writer.println("Access-Control-Allow-Origin: null");
                        writer.println();
                        writer.close();
                    }
                } catch (IOException e) {
                    callback.onStateUpdate("socket closed");
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
