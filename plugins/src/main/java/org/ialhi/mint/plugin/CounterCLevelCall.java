package org.ialhi.mint.plugin;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

/**
 * User: Yoann Moranville
 * Date: Feb 9, 2011
 *
 * @author Yoann Moranville
 */
public class CounterCLevelCall extends ExtensionFunctionCall {
    private int counter = 0;
    private int maxCounter;

    public Sequence call(XPathContext xPathContext, Sequence[] arguments) throws XPathException {
        count();
        return (new StringValue(""));
    }

    public void count(){
        counter++;
    }

    public int getCounter(){
        return counter;
    }

    public int getMaxCounter(){
        return maxCounter;
    }

    public void initializeCounter(int max) {
        maxCounter = max;
        counter = 0;
    }
}