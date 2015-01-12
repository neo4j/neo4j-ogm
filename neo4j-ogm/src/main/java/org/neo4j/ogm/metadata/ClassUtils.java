package org.neo4j.ogm.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class ClassUtils {

    @SuppressWarnings("serial")
    private static final Map<String, Class<?>> PRIMITIVE_TYPE_MAP = new HashMap<String, Class<?>>() {{
        put("Z", Boolean.TYPE);
        put("B", Byte.TYPE);
        put("C", Character.TYPE);
        put("D", Double.TYPE);
        put("F", Float.TYPE);
        put("I", Integer.TYPE);
        put("J", Long.TYPE);
        put("S", Short.TYPE);
    }};

    /**
     * Return the reified class for the parameter of a JavaBean setter from the setter signature
     */
    public static Class<?> getType(String setterDescriptor) {

        int p = setterDescriptor.indexOf("(");
        int q = setterDescriptor.indexOf(")");

        if (!setterDescriptor.contains("[")) {
            if (setterDescriptor.endsWith(";)V")) {
                q--;
            }
            if (setterDescriptor.startsWith("(L")) {
                p++;
            }
        }
        String typeName = setterDescriptor.substring(p + 1, q).replace("/", ".");
        if (typeName.length() == 1) {
            return PRIMITIVE_TYPE_MAP.get(typeName);
        }

        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
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
