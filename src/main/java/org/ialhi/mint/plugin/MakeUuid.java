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

import java.util.UUID;

/**
 * Created by Josh on 3/11/16.
 */
public class MakeUuid extends ExtensionFunctionDefinition {
    private static final StructuredQName qName =
            new StructuredQName("",
                    "http://www.socialhistoryservices.org/",
                    "makeuuid");

    @Override
    public StructuredQName getFunctionQName() {
        return qName;
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return 0;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 0;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] { SequenceType.EMPTY_SEQUENCE };
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new MakeUuidCall();
    }

    public static class MakeUuidCall extends ExtensionFunctionCall {

        @Override
        public SequenceIterator call(SequenceIterator[] arguments, XPathContext xPathContext) throws XPathException {
            Item item = new StringValue(UUID.randomUUID().toString());
            return SingletonIterator.makeIterator(item);
        }
    }
}
