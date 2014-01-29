package me.horzwxy.app.wordbook.swing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;

/**
 * Create output HTML file.
 */
public class HTMLCreator {

    private Document document;
    private Node articleNode;
    private int port;

    public HTMLCreator(int port) {
        this.port = port;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        document = null;
        try {
            document = db.parse(new File("model.html"));
            articleNode = document.getElementsByTagName("div").item(0);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSentence(String sentence, List<String> emphasizedWords) {
        Node sentenceNode = createSentenceNode(sentence, emphasizedWords);
        articleNode.appendChild(sentenceNode);
    }

    public void outputDocument(String filename) {
        // Use a Transformer for output
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }

        DOMSource source = new DOMSource(document);
        StreamResult result = null;
        try {
            result = new StreamResult(new FileOutputStream(new File(filename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private Node createSentenceNode(String sentence, List<String> emphasizedWords) {
        Element sentenceNode = document.createElement("p");
        String[] words = sentence.split("[ ,\"]");
        for(String word : words) {
            if(emphasizedWords.contains(word)) {
                Element element = document.createElement("em");
                element.setTextContent(word);
                element.setAttribute("style", "color:red");
                element.setAttribute("onclick", "addNewWord(\"" + word + "\", " + port + ")");
                sentenceNode.appendChild(element);
                sentenceNode.appendChild(document.createTextNode(" "));
            }
            else {
                Node element = document.createTextNode(word + " ");
                sentenceNode.appendChild(element);
            }
        }

        return sentenceNode;
    }
}
