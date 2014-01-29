package me.horzwxy.app.wordbook.swing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
            NodeList divs = document.getElementsByTagName("div");
            articleNode = getArticleNode(divs);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Node getArticleNode(NodeList divs) {
        for(int i = 0; i < divs.getLength(); i++) {
            for(int j = 0; j < divs.item(i).getAttributes().getLength(); j++) {
                String attrName = divs.item(i).getAttributes().item(j).getNodeName();
                String attrValue = divs.item(i).getAttributes().item(j).getNodeValue();

                if(attrName.equals("id") && attrValue.equals("article")) {
                    return divs.item(i);
                }
            }
        }

        return null;
    }

    public void addSentence(String sentence, List<String> emphasizedWords) {
        Node sentenceNode = createSentenceNode(sentence, emphasizedWords);
        articleNode.appendChild(sentenceNode);
    }

    public void outputDocument(File outputFile) {
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
            result = new StreamResult(new FileOutputStream(outputFile));
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
