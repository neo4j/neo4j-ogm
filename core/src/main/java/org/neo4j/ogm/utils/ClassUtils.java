/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.utils;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.service.ResourceService;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public abstract class ClassUtils {

    private static Map<String, Class<?>> descriptorTypeMappings = new HashMap<>();

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
     * Return the reified class for the parameter of a parameterised setter or field from the parameter signature
     *
     * @param descriptor parameter descriptor
     * @return reified class for the parameter
     * @throws NullPointerException if invoked with <code>null</code>
     */
    public static Class<?> getType(String descriptor) {
        if (descriptorTypeMappings.containsKey(descriptor)) {
            return descriptorTypeMappings.get(descriptor);
        }
        Class<?> type = computeType(descriptor);
        descriptorTypeMappings.put(descriptor, type);
        return type;
    }

    /**
     * Return the reified class for the parameter of a parameterised setter or field from the parameter signature.
     * Return null if the class could not be determined
     *
     * @param descriptor parameter descriptor
     * @return reified class for the parameter or null
     * @throws NullPointerException if invoked with <code>null</code>
     */
    public static Class<?> getTypeOrNull(String descriptor) {
        if (descriptorTypeMappings.containsKey(descriptor)) {
            return descriptorTypeMappings.get(descriptor);
        }
        Class<?> type;
        try {
            type = computeType(descriptor);
        }
        catch (Exception e) {
            //return null and swallow the exception
            return null;
        }
        descriptorTypeMappings.put(descriptor, type);
        return type;
    }

    private static Class<?> computeType(String descriptor) {
        // user has defined a wild card parameter / return type in a generic signature.
        // we can't handle this.
        if (descriptor.startsWith("+") || descriptor.startsWith("-") || descriptor.contains(":")) {
            throw new RuntimeException("The use of wild cards in generic return types of method parameters is not supported");
        }

        // generic type signature <S extends T> we want to get T
        if (descriptor.contains(":")) {
            return getType(descriptor.substring(descriptor.indexOf(":") + 1));
        }

        if(descriptor.startsWith("()")) {
            return getType(descriptor.substring(2));
        }

        int p = descriptor.indexOf("(");
        int q = descriptor.indexOf(")");

        if (!descriptor.contains("[")) {
            if (descriptor.endsWith(";)V")) {
                q--;
            }
            if (descriptor.startsWith("(L")) {
                p++;
            }
            if(descriptor.startsWith("L")) { //handles descriptors of the format Ljava/lang/Byte;
                p++;
                q = descriptor.length()-1;
            }
        }
        if(descriptor.startsWith("[")) { //handles descriptors of the format [F
            p = 0;
            q = 2;
        }
        if(descriptor.startsWith("[L")) { //handles descriptors of the format [Ljava/lang/Float;
            p = 1;
            q = descriptor.length()-1;
        }
        if(descriptor.length()==1) { //handles descriptors of the format I
            q=1;
        }
        if(q == p+1) { //handles descriptors of the format ()Lpackage/Class;
            p = q + 1;
            q = descriptor.length() - 1;
        }
        String typeName = descriptor.substring(p + 1, q).replace("/", ".");
        if (typeName.length() == 1) {
            return PRIMITIVE_TYPE_MAP.get(typeName);
        }

        // if class is parametrized, obtain simple class signature.

        if (typeName.contains("<T")) {
            typeName = typeName.substring(0, typeName.indexOf("<T"));
        }

        if (typeName.contains("<L")) {
            typeName = typeName.substring(0, typeName.indexOf("<L"));
        }

        try {
            return MetaDataClassLoader.loadClass(typeName);//Class.forName(typeName);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a list of unique elements on the classpath as File objects, preserving order.
     * Classpath elements that do not exist are not returned.
     *
     * Uses the ResourceService to resolve classpath URLs. The "file" and "jar" protocols are
     * supported by default. Other protocols, for example "vfs", can be handled by writing
     * an appropriate resolver and registering it with the ServiceLoader mechanism
     * as an instance of {@link org.neo4j.ogm.classloader.ResourceResolver}
     *
     * @param classPaths classpaths to be included
     * @return {@link List} of unique {@link File} objects on the classpath
     */
    public static Set<File> getUniqueClasspathElements(List<String> classPaths) {
        Set<File> pathFiles = new HashSet<>();
        for(String classPath : classPaths) {
            try {
                Enumeration<URL> resources = ClassUtils.class.getClassLoader().getResources(classPath.replace(".","/"));
                while(resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    pathFiles.add( ResourceService.resolve( url ));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return pathFiles;
    }

}
