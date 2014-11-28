package org.neo4j.ogm.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class ClassUtils {

    /**
     * Return the reified class for the parameter of a JavaBean setter from the setter signature
     */
    public static Class getType(String descriptor) {

        int p = descriptor.indexOf("(");
        int q = descriptor.indexOf(")");

        if (!descriptor.contains("[")) {
            if (descriptor.endsWith(";)V")) {
                q--;
            }
            if (descriptor.startsWith("(L")) {
                p++;
            }
        }
        String typeName = descriptor.substring(p + 1, q).replace("/", ".");
        try {
            return Class.forName(typeName);
        } catch (Exception e) {
            // FIXME: this fails for setters that take primitives
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a list of unique elements on the classpath as File objects, preserving order.
     * Classpath elements that do not exist are not returned.
     */
    public static ArrayList<File> getUniqueClasspathElements() {
        String[] pathElements = System.getProperty("java.class.path").split(File.pathSeparator);
        HashSet<String> pathElementsSet = new HashSet<>();
        ArrayList<File> pathFiles = new ArrayList<>();
        for (String pathElement : pathElements) {
            if (pathElementsSet.add(pathElement)) {
                File file = new File(pathElement);
                if (file.exists()) {
                    pathFiles.add(file);
                }
            }
        }
        return pathFiles;
    }

}
