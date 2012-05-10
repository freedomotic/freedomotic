package it.freedomotic.util;

import it.freedomotic.app.Freedomotic;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DOMValidateDTD {

    public static String validate(File xmlFile, String absolutePathToDtd) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder;

            documentBuilder = factory.newDocumentBuilder();

            Document doc = documentBuilder.parse(xmlFile);
            DOMSource source = new DOMSource(doc);

            //now use a transformer to add the DTD element declaration at top
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, new File(absolutePathToDtd).getAbsolutePath());
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);


            //create a validating factory with error handling
            factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new org.xml.sax.ErrorHandler() {

                public void fatalError(SAXParseException fatal) throws SAXException {
                    //enable when validator feature is fully implemented
                    //Freedomotic.logger.warning(fatal.getMessage());
                }

                public void error(SAXParseException e) throws SAXParseException {
                    //enable when validator feature is fully implemented
                    //Freedomotic.logger.warning("Error at line " + e.getLineNumber() + ". " + e.getMessage());
                }

                public void warning(SAXParseException err) throws SAXParseException {
                    //enable when validator feature is fully implemented
                    //Freedomotic.logger.warning("Warning at line " + err.getLineNumber() + ". " + err.getMessage());
                }
            });
            //finally parse the result. 
            //this will throw an exception if the doc is invalid
            documentBuilder.parse(new InputSource(new StringReader(writer.toString())));
            return writer.toString();
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        } catch (TransformerConfigurationException ex) {
            throw new RuntimeException(ex);
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }
}