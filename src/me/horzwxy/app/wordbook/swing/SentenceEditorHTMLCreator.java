package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.analyzer.WordLibrary;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by horz on 1/29/14.
 */
public class SentenceEditorHTMLCreator {

    private Document document;
    private int port;

    public SentenceEditorHTMLCreator(int port, WordLibrary wordLibrary) {
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
            document = db.parse(new File("sentence_editor.html"));
            NodeList divs = document.getElementsByTagName("ul");
            for(WordState state : WordState.values()) {
                Node node = getNodeWithId(state.name().toLowerCase() + "_list", divs);
                if(node != null) {
                    updateNode(node, state, wordLibrary);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Node getNodeWithId(String id, NodeList divs) {
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

    private void updateNode(Node node, WordState state, WordLibrary wordLibrary) {
        Collection<Word> words = wordLibrary.getWords(state);
        for(Word word : words) {
            Element liElement = document.createElement("li");
            node.appendChild(liElement);

            Element wordSpan = document.createElement("span");
            liElement.appendChild(wordSpan);
            wordSpan.appendChild(document.createTextNode(word.getContent()));

            Element sentenceList = document.createElement("ul");
            liElement.appendChild(sentenceList);

            for(String sentence : word.getSentences()) {
                Element sentenceNode = document.createElement("li");
                sentenceList.appendChild(sentenceNode);

                Element sentenceContent = document.createElement("span");
                sentenceContent.appendChild(document.createTextNode(sentence));
                sentenceContent.setAttribute("onclick", "editSentence('" + word.getContent() + "', " + sentence.hashCode() + ", " + port + ")");
                sentenceNode.appendChild(sentenceContent);

                System.out.println();
            }
        }
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
}
