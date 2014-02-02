package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * A tool to create XML document.
 */
public class XMLCreator {

    public static boolean generateWordList(Map<WordState, Collection<Word>> wordListMap, Map<String, String> serverAttrs, File outputFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        if(db == null) {
            return false;
        }
        Document document = db.newDocument();

        // create root node
        Element rootElement = document.createElement("wordlist");
        document.appendChild(rootElement);
        if(serverAttrs != null) {
            for(String attrName : serverAttrs.keySet()) {
                rootElement.setAttribute(attrName, serverAttrs.get(attrName));
            }
        }

        Node xslRef = document.createProcessingInstruction
                ("xml-stylesheet", "type=\"text/xsl\" href=\"/home/horz/workspace-java/wordbook_swing/models/wordlist.xsl\"");
        document.insertBefore(xslRef, rootElement);

        // append words
        for(WordState state : wordListMap.keySet()) {
            Element sublistNode = document.createElement("word_sublist");
            sublistNode.setAttribute("type", state.name());

            for(Word word : wordListMap.get(state)) {
                Node wordNode = document.createElement("word");
                Node contentNode = document.createElement("content");
                contentNode.appendChild(document.createTextNode(word.getContent()));
                wordNode.appendChild(contentNode);

                for(String sentence : word.getSentences()) {
                    Node sentenceNode = document.createElement("sentence");
                    sentenceNode.appendChild(document.createTextNode(sentence));
                    wordNode.appendChild(sentenceNode);
                }

                sublistNode.appendChild(wordNode);
            }

            rootElement.appendChild(sublistNode);
        }


        // Use a Transformer for output
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
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

        return true;
    }
}
