package org.ialhi.mint.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

class PluginLoader {


    protected final Logger log = Logger.getLogger(getClass());


    /**
     * Recursive method. Lists content of directory. If it finds a jar, it adds it to the list, if is finds
     * a directory, it calls itself with that directory as argument
     *
     * @param f           - file handle representing a directory
     * @param jarNameList - List to which we're adding Jar names
     * @throws java.io.IOException If problems processing Jars or directories
     */
    public void scanDirectory(File f, List jarNameList) throws IOException {

        File[] children = f.listFiles();

        for (File aChildren : children) {

            boolean javaArchive = false;
            String[] extensions = {"jar"};

            for (String extension : extensions) {
                if (aChildren.isFile() && aChildren.getName().endsWith("." + extension)) {
                    javaArchive = true;
                }
            }

            if (javaArchive) {
                String name = aChildren.getCanonicalPath();
                jarNameList.add(name);
            } else if (aChildren.isDirectory()) {
                scanDirectory(aChildren, jarNameList);
            } else {
                log.info("Ignore: " + aChildren.getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) throws IOException {

        assert args.length != 0;

        final List<String> jarNameList = new ArrayList<>();
        for (String plugin_folder : args) {
            final PluginLoader pluginLoader = new PluginLoader();
            pluginLoader.scanDirectory(new File(plugin_folder), jarNameList);

        }
    }
}