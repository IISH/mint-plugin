package org.ialhi.mint.plugin;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;

import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.soap.*;
import java.io.IOException;

public class GetQuickPidRequest extends ExtensionFunctionDefinition {
    private static final StructuredQName qName =
            new StructuredQName("ialhi",
                    "http://www.socialhistoryportal.org/functions",
                    "getQuickPidRequest");

    @Override
    public StructuredQName getFunctionQName() {
        return qName;
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return 3;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 3;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{
                SequenceType.SINGLE_STRING,
                SequenceType.SINGLE_STRING,
                SequenceType.SINGLE_STRING
        };
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new GetQuickPidRequestCall();
    }

    public static class GetQuickPidRequestCall extends ExtensionFunctionCall {

        @Override
        public SequenceIterator call(SequenceIterator[] arguments, XPathContext xPathContext) throws XPathException {
            StringValue na = (StringValue) arguments[0].next();
            StringValue localIdentifier = (StringValue) arguments[1].next();
            StringValue uri = (StringValue) arguments[2].next();

            String pid = null;
            try {
                pid = registerPid(na, localIdentifier, uri);
            } catch (SOAPException e) {
                pid = "Invalid PID";
            }

            pid = "http://hdl.handle.net/" + pid;
            Item item = new StringValue(pid);
            return SingletonIterator.makeIterator(item);
        }

        private static String registerPid(StringValue na, StringValue localIdentifier, StringValue uri) throws SOAPException {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            String url = "https://pid.socialhistoryservices.org/pid.wsdl";
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(na.toString(), localIdentifier.toString(), uri.toString()), url);

            SOAPBody soapBody = soapResponse.getSOAPBody();
            NodeList nl = soapBody.getElementsByTagNameNS("http://pid.socialhistoryservices.org/", "pid");
            Node node = nl.item(0);
            String pid = node.getFirstChild().getNodeValue();

            soapConnection.close();

            return pid;
        }

        private static SOAPMessage createSOAPRequest(String naValue, String localIdValue, String resolveUrlValue) throws SOAPException {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            String pidKey = "";

            naValue = naValue.replaceAll("^\"|\"$", "");
            localIdValue = localIdValue.replaceAll("^\"|\"$", "");
            resolveUrlValue = resolveUrlValue.replaceAll("^\"|\"$", "");

            String serverURI = "http://pid.socialhistoryservices.org/";

            PropertyLoader propertyLoader = new PropertyLoader();
            try {
                pidKey = propertyLoader.getProperties().getProperty("pidkey");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // SOAP Envelope
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.addNamespaceDeclaration("pid", serverURI);

                /*
                Constructed SOAP Request Message:
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:pid="http://pid.socialhistoryservices.org/">
                    <soapenv:Body>
                        <pid:GetQuickPidRequest>
                            <pid:na>$naValue</pid:na>
                            <pid:localIdentifier>$localIdValue</pid:localIdentifier>
                            <pid:resolveUrl>$resolveUrlValue</pid:resolveUrl>
                        </pid:GetQuickPidRequest>
                    </soapenv:Body>
                </soapenv:Envelope>
                 */

            // SOAP Body
            SOAPBody soapBody = envelope.getBody();
            SOAPElement getQuickPidRequest = soapBody.addChildElement("GetQuickPidRequest", "pid");
            SOAPElement na = getQuickPidRequest.addChildElement("na", "pid");
            na.addTextNode(naValue);
            SOAPElement localIdentifier = getQuickPidRequest.addChildElement("localIdentifier", "pid");
            localIdentifier.addTextNode(localIdValue);
            SOAPElement resolveUrl = getQuickPidRequest.addChildElement("resolveUrl", "pid");
            resolveUrl.addTextNode(resolveUrlValue);

            MimeHeaders hd = soapMessage.getMimeHeaders();
            hd.addHeader("Authorization", "bearer " + pidKey);

            soapMessage.saveChanges();
            String message = soapBody.getValue();
            return soapMessage;
        }
    }
}
