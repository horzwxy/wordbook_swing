package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.model.AnalyzeResult;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;
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
public class AnalyseResultHTMLCreator {

    private Document document;
    private Node articleNode;
    private int port;

    public AnalyseResultHTMLCreator(int port) {
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
            articleNode = getNodeById(divs, "article");
            NodeList spans = document.getElementsByTagName("span");
            Node portNode = getNodeById(spans, "port");
            portNode.appendChild(document.createTextNode(port + ""));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Node getNodeById(NodeList divs, String id) {
        for(int i = 0; i < divs.getLength(); i++) {
            for(int j = 0; j < divs.item(i).getAttributes().getLength(); j++) {
                String attrName = divs.item(i).getAttributes().item(j).getNodeName();
                String attrValue = divs.item(i).getAttributes().item(j).getNodeValue();

                if(attrName.equals("id") && attrValue.equals(id)) {
                    return divs.item(i);
                }
            }
        }

        return null;
    }

    public void addSentence(String sentence, List<Word> emphasizedWords, List<AnalyzeResult> results) {
        Node sentenceNode = createSentenceNode(sentence, emphasizedWords, results);
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

    private Node createSentenceNode(String sentence, List<Word> emphasizedWords, List<AnalyzeResult> results) {
        Element sentenceNode = document.createElement("p");
        String[] words = sentence.split("[ ,\"]");
        for(String word : words) {
            Word wordExample = new Word(word, null);
            if(emphasizedWords.contains(wordExample)) {
                Word wordInstance = emphasizedWords.get(emphasizedWords.indexOf(wordExample));
                Element element = document.createElement("em");
                element.setTextContent(word);
                if(wordInstance.getState().equals(WordState.UNTRACKED)) {
                    element.setAttribute("style", "color:red");
                }
                else if(wordInstance.getState().equals(WordState.UNRECOGNIZED)) {
                    element.setAttribute("style", "color:blue");
                }
                else if(wordInstance.getState().equals(WordState.UNFAMILIAR)) {
                    element.setAttribute("style", "color:orange");
                }

                AnalyzeResult analyzeResult = null;
                for(AnalyzeResult result : results) {
                    if(result.getWord() == wordInstance) {
                        analyzeResult = result;
                        break;
                    }
                }

                String formsString = "[";
                for(String form : analyzeResult.getPossibleForms()) {
                    formsString += "'" + form + "',";
                }
                formsString = formsString.substring(0, formsString.length() - 1) + "]";

                element.setAttribute("onclick", "addNewWord(\"" + word + "\"," + formsString + ")");
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
