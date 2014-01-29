package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.analyzer.WordLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by horz on 1/29/14.
 */
public class LocalServer extends ServerSocket {

    private Thread serverThread;
    private WordLibrary wordLibrary;

    public LocalServer(int port, WordLibrary wordLibrary) throws IOException {
        super(port);
        this.wordLibrary = wordLibrary;
    }

    public void start() {
        serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    Socket client = accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    if(reader.ready()) {
                        handleRequest(new SimpleHttpRequest(reader.readLine()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        serverThread.start();
    }

    public void stop() {
        this.stop();
        serverThread.interrupt();
    }

    private void handleRequest(SimpleHttpRequest request) {
        if(request.pattern.equals("addnewword")) {

        }
    }

    private class SimpleHttpRequest {

        private String pattern;
        private Map<String, String> parameters;

        private SimpleHttpRequest(String requestHead) {
            String[] parts = requestHead.split(" ")[1].split("/");
            pattern = parts[parts.length - 1].split("\\?")[0];
            parameters = new HashMap<String, String>();
            String[] pairs = parts[parts.length - 1].split("\\?")[1].split("&");
            for(String pair : pairs) {
                parameters.put(pair.split("=")[0], pair.split("=")[1]);
            }
        }

        public String getPattern() {
            return pattern;
        }

        public String getParameter(String key) {
            return parameters.get(key);
        }
    }
}
