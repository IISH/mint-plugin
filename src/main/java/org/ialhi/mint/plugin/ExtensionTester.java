package org.ialhi.mint.plugin;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;

public class ExtensionTester {

    public static void main(String[] args) throws IOException, URISyntaxException, TransformerException {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

        TransformerFactory tFactory = TransformerFactory.newInstance();

        if(tFactory instanceof TransformerFactoryImpl) {
            TransformerFactoryImpl tFactoryImpl = (TransformerFactoryImpl) tFactory;
            Configuration saxonConfig = tFactoryImpl.getConfiguration();
            saxonConfig.registerExtensionFunction(new MakeUuid());
            saxonConfig.registerExtensionFunction(new GetQuickPidRequest());
        }

        Source xslt = new StreamSource(new File("src/main/resources/testMintExtensions.xsl"));
        Transformer transformer = tFactory.newTransformer(xslt);

        Source xml = new StreamSource(new File("src/main/resources/testMintExtensionsInput.xml"));
        transformer.transform(xml, new StreamResult(new File("src/main/resources/testMintExtensionsResults.xml")));
    }

}