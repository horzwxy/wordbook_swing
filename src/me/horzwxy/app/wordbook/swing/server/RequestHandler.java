package me.horzwxy.app.wordbook.swing.server;

/**
 * Created by horz on 1/29/14.
 */
interface RequestHandler {

    void handleRequest(SimpleHttpRequest request, LocalServer.ServerCallback callback);
}
