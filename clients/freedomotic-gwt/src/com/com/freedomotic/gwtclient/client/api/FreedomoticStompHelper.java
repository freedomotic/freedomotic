package com.freedomotic.gwtclient.client.api;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

//
/**
 * @author gpt h elper class with methods to parse messages from the stomp queue
 */
public class FreedomoticStompHelper {

    public static Payload parseMessage(String message) {
        final Payload payload = new Payload();

        Document messageDom = XMLParser.parse(message);
        NodeList statements = messageDom.getElementsByTagName("com.freedomotic.reactions.Statement");
        for (int i = 0; i < statements.getLength(); i++) {
            Element statement = (Element) statements.item(i);
            payload.enqueueStatement(parseStatement(statement));
        }
        return payload;
    }

    private static Statement parseStatement(Element statementNode) {
        Statement statement = new Statement();
        statement.setLogical(getElementTextValue(statementNode, "logical"));
        statement.setAttribute(getElementTextValue(statementNode, "attribute"));
        statement.setOperand(getElementTextValue(statementNode, "operand"));
        statement.setValue(getElementTextValue(statementNode, "value"));
        return statement;

    }

    /**
     * Utility method to return the values of elements of the form <myTag>tag
     * value</myTag>
     */
    private static String getElementTextValue(Element parent, String elementTag) {
        // If the xml is not coming from a known good source, this method would
        // have to include safety checks.
        return parent.getElementsByTagName(elementTag).item(0).getFirstChild()
                .getNodeValue();
    }
}
