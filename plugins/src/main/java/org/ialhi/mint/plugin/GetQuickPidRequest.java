package org.ialhi.mint.plugin;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.*;
import java.util.LinkedHashMap;


public class GetQuickPidRequest extends ExtensionFunctionDefinition {
    private static final StructuredQName qName =
            new StructuredQName("ialhi",
                    "http://www.socialhistoryportal.org/functions",
                    "getQuickPidRequest");

    private static int CACHE_LIMIT = 50;

    /**
     * cache
     * <p>
     * Remembers a number of responses. The total is determined by CACHE_LIMIT.
     * Once reached, the cache is cleared.
     * <p>
     * The key-value for:
     * na:localIdentifier:uri and the pid (na/identifier)
     */
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<>(CACHE_LIMIT);


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
        public Sequence call(XPathContext xPathContext, Sequence[] arguments) throws XPathException {

            final String na = arguments[0].head().getStringValue();
            final String localIdentifier = arguments[1].head().getStringValue();
            final String uri = arguments[2].head().getStringValue();
            final String pid = registerPid(na, localIdentifier, uri);
            return StringValue.makeStringValue(getHandleResolver() + pid);
        }
    }

    private static String registerPid(String na, String localIdentifier, String uri) {

        final String key = na + ":" + localIdentifier + ":" + uri;
        if (cache.containsKey(key))
            return cache.get(key);

        String pid = null;
        try {
            final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            String url = getPidWebserviceEndpoint() + "pid.wsdl";
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(na, localIdentifier, uri), url);

            SOAPBody soapBody = soapResponse.getSOAPBody();
            SOAPFault sf = soapBody.getFault();
            if (sf == null) {
                NodeList nl = soapBody.getElementsByTagNameNS("http://pid.socialhistoryservices.org/", "pid");
                Node node = nl.item(0);
                pid = node.getFirstChild().getNodeValue();
            } else {
                throw new SOAPException(sf.getFaultString());
            }
            soapConnection.close();
        } catch (SOAPException e) {
            e.printStackTrace();
        }

        if (pid == null || pid.isEmpty()) {
            try {
                throw new Exception("The webservice returned an empty PID value.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (cache.size() >= CACHE_LIMIT) {
                cache.remove(cache.keySet().iterator().next()); // remove the first entry.
            }
            cache.put(key, pid);
        }

        return pid;
    }

    private static SOAPMessage createSOAPRequest(String naValue, String localIdValue, String resolveUrlValue) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        naValue = naValue.replaceAll("^\"|\"$", "");
        localIdValue = localIdValue.replaceAll("^\"|\"$", "");
        resolveUrlValue = resolveUrlValue.replaceAll("^\"|\"$", "");


        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("pid", "http://pid.socialhistoryservices.org/");

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
        hd.addHeader("Authorization", "bearer " + getPidWebserviceApiKey());

        soapMessage.saveChanges();
        return soapMessage;
    }

    private static String getHandleResolver() {
        return System.getProperty("xsltplugin.getquickpidrequest.handle_resolver", "http://hdl.handle.net/");
    }

    private static String getPidWebserviceApiKey() {
        return System.getProperty("xsltplugin.getquickpidrequest.apikey");
    }

    private static String getPidWebserviceEndpoint() {
        return System.getProperty("xsltplugin.getquickpidrequest.endpoint", "http://localhost/secure");
    }
}
