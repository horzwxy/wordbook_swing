package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.swing.ui.FrameModel;

/**
 * Created by horz on 1/28/14.
 */
public class Launcher {

    public static void main(String[] args) {
        FrameModel.getFrame().setVisible(true);
    }

//    public static void main(String[] args) throws Exception {
//        HttpServer server = HttpServer.create(new InetSocketAddress(7890), 0);
//        server.createContext("/test", new MyHandler());
//        server.setExecutor(null); // creates a default executor
//        server.start();
//    }
//
//    static class MyHandler implements HttpHandler {
//        public void handle(HttpExchange t) throws IOException {
//            System.out.println("query " + t.getRequestURI().getQuery());
//            Headers reqHeaders = t.getRequestHeaders();
//            for(String name : reqHeaders.keySet()) {
//                System.out.println(name + ":" + reqHeaders.get(name));
//            }
//            Headers respHeaders = t.getResponseHeaders();
//            for(String name : respHeaders.keySet()) {
//                System.out.println("resp headers " + respHeaders.get(name));
//            }
//            System.out.println("attr " + t.getAttribute("param1"));
//
//            String response = "This is the response";
//            t.sendResponseHeaders(200, response.length());
//            OutputStream os = t.getResponseBody();
//            os.write(response.getBytes());
//            os.close();
//        }
//    }
}
