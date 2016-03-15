package org.ialhi.mint.plugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {
    Properties prop;
    InputStream inputStream;

    public Properties getProperties() throws IOException {
        prop = new Properties();
        String propFileName = "config.properties";

        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
            return prop;
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
    }

    public static void main(String[] args) throws IOException {
        P
    }
}
