package me.horzwxy.app.wordbook.swing;

import me.horzwxy.app.wordbook.analyzer.WordLibrary;
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
import java.io.*;
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

                if(state != WordState.FAMILIAR) {
                    for(String sentence : word.getSentences()) {
                        Element sentenceNode = document.createElement("sentence");
                        sentenceNode.appendChild(document.createTextNode(sentence));
                        sentenceNode.setAttribute("hash", sentence.hashCode() + "");
                        wordNode.appendChild(sentenceNode);
                    }
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

    public static String generateMarkArticle(WordLibrary wordLibrary, File articleFile) {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                + "<en-note>";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(articleFile));
            String line = reader.readLine();

            StringBuffer sb = new StringBuffer();

            while (line != null) {
                result += "<p>";
                int lineLength = line.length();
                char lastChar = '\0';
                for(int i = 0; i < lineLength; i++) {
                    char c = line.charAt(i);
                    if(Character.isLetter(c)) {
                        sb.append(c);
                    }
                    else {
                        // if not a word-ending mark
                        if(c != ' ' && c != '.'
                                && c != '\"' && c != ','
                                && c != ':' && c != ';'
                                && c != ')' && c != '}'
                                && c != ']' && c != '>') {
                            if(c == '&') {
                                sb.append("&amp;");
                                continue;
                            }
                            else if(c == '<') {
                                result += "&lt;";
                                continue;
                            }
                            else if(c == '(' || c == '{' || c == '[') {
                                result += c;
                                continue;
                            }
                            else {
                                sb.append(c);
                            }
                        }
                        else {
                            lastChar = c;
                        }

                        if(sb.length() != 0) {
                            String word = sb.toString();
                            WordState state = wordLibrary.getWordState(word.toLowerCase());
                            if(state == WordState.UNRECOGNIZED) {
                                result += "<span style=\"color:red\">" + word + "</span>";
                            }
                            else if (state == WordState.UNFAMILIAR){
                                result += "<span style=\"color:blue\">" + word + "</span>";
                            }
                            else {
                                result += word;
                            }
                        }
                        if(lastChar != '\0') {
                            if(lastChar == '>') {
                                result += "&gt;";
                            }
                            else {
                                result += lastChar;
                            }
                            lastChar = '\0';
                        }
                        sb = new StringBuffer();
                    }
                }
                if(sb.length() != 0) {
                    String word = sb.toString();
                    WordState state = wordLibrary.getWordState(word.toLowerCase());
                    if(state == WordState.UNRECOGNIZED) {
                        result += "<span style=\"color:red\">" + word + "</span>";
                    }
                    else if (state == WordState.UNFAMILIAR){
                        result += "<span style=\"color:blue\">" + word + "</span>";
                    }
                    else {
                        result += word;
                    }
                    sb = new StringBuffer();
                }
                result += "</p>";
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result += "</en-note>";
        return result;
    }
}
