package me.horzwxy.app.wordbook.swing.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by horz on 1/29/14.
 */
class SimpleHttpRequest {

    private String pattern;
    private Map<String, String> parameters;

    SimpleHttpRequest(String requestHead) {
        String[] parts = requestHead.split(" ")[1].split("/");
        pattern = parts[parts.length - 1].split("\\?")[0];
        parameters = new HashMap<String, String>();
        String[] pairs = parts[parts.length - 1].split("\\?")[1].split("&");
        for(String pair : pairs) {
            parameters.put(pair.split("=")[0], pair.split("=")[1]);
        }
    }

    String getPattern() {
        return pattern;
    }

    String getParameter(String key) {
        return parameters.get(key);
    }
}
