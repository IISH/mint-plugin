package org.ialhi.mint.plugin;

import org.junit.Assert;
import org.junit.Test;


public class PluginLoaderTest {

    @Test
    public void readInClasses() {

        System.setProperty("plugins", "./bootstrap/src/test/resources");
        Assert.assertTrue(PluginLoader.getPrefixNamespace().isEmpty());
        PluginLoader.getTransformerFactory();
        Assert.assertTrue(PluginLoader.getPrefixNamespace().size() == 2);

    }

}