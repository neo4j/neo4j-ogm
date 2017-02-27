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
import java.util.*;

import org.neo4j.ogm.metadata.classloader.ClassLoaderResolver;
import org.neo4j.ogm.metadata.classloader.MetaDataClassLoader;
import org.neo4j.ogm.metadata.classloader.ResourceResolver;
import org.neo4j.ogm.exception.ServiceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public abstract class ClassUtils {

    private static final Logger logger = LoggerFactory.getLogger( ClassUtils.class );


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
     * Return the reified class for the parameter of a parameterised setter or field from the parameter signature.
     * Return null if the class could not be determined
     *
     * @param descriptor parameter descriptor
     * @return reified class for the parameter or null
     */
    public static Class<?> getType(String descriptor) {
        if (descriptorTypeMappings.containsKey(descriptor)) {
            return descriptorTypeMappings.get(descriptor);
        }
        Class<?> type;
        try {
            type = computeType(descriptor);
        }
        catch (Throwable t) {
            //return null and swallow the exception
            return null;
        }
        descriptorTypeMappings.put(descriptor, type);
        return type;
    }

    private static Class<?> computeType(String descriptor) throws ClassNotFoundException {

        if (descriptor == null) {
            return null;
        }

        // handle Void
        if (descriptor.equals("V")) {
            return Void.class;
        }


        if (descriptor.contains(":")) {
            return getType(descriptor.substring(descriptor.indexOf(":") + 1));
        }

        // generic types and wildcards are replaced by Object in the compiler
        if (descriptor.startsWith("+") || descriptor.startsWith("-") || descriptor.startsWith("*")) {
            return Object.class;
        }

        // function returns - strip off, and pass in the just the type part
        if(descriptor.startsWith("()")) {
            return getType(descriptor.substring(2));
        }

        // type is a function parameter?
        int p = descriptor.indexOf("(");
        int q = descriptor.indexOf(")");

        // if the parameter is not an array of some type
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

        // type is an array?
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

        // some generic type we've lost through type erasure. JVM will use Object. so will we
        if (p == -1 && q == -1) {
            return Object.class;
        }
        // construct a type name
        String typeName = descriptor.substring(p + 1, q).replace("/", ".");

        // is it a primitive?
        if (typeName.length() == 1) {
            return PRIMITIVE_TYPE_MAP.get(typeName);
        }

        // if class is parametrized, obtain simple class signature.
        if (typeName.contains("<")) {
            typeName = typeName.substring(0, typeName.indexOf("<"));
        }

        return MetaDataClassLoader.loadClass(typeName);
    }

    /**
     * Get a list of unique elements on the classpath as File objects, preserving order.
     * Classpath elements that do not exist are not returned.
     *
     * Uses the ResourceService to resolve classpath URLs. The "file" and "jar" protocols are
     * supported by default. Other protocols, for example "vfs", can be handled by writing
     * an appropriate resolver and registering it with the ServiceLoader mechanism
     * as an instance of {@link org.neo4j.ogm.metadata.classloader.ResourceResolver}
     *
     * @param classPaths classpaths to be included
     * @return {@link List} of unique {@link File} objects on the classpath
     */
    public static Set<File> getUniqueClasspathElements(List<String> classPaths) {
        Set<File> pathFiles = new HashSet<>();
        for(String classPath : classPaths) {
            try {
				Enumeration<URL> resources = ClassLoaderResolver.resolve().getResources(classPath.replace(".","/"));
                while(resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    pathFiles.add( resolve( url ));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return pathFiles;
    }


    public static File resolve( URL url ) throws Exception {

        ServiceLoader< ResourceResolver > serviceLoader = ServiceLoader.load( ResourceResolver.class );

        for (ResourceResolver resourceResolver : serviceLoader) {
            try {
                File file = resourceResolver.resolve(url);
                if (file != null) {
                    return file;
                }
            } catch (ServiceConfigurationError sce) {
                logger.warn("{}, reason: {}", sce.getLocalizedMessage(), sce.getCause());
            }
        }

        throw new ServiceNotFoundException("Resource: " + url.toExternalForm());
    }

}
