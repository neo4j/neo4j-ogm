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

import org.neo4j.ogm.exception.ServiceNotFoundException;
import org.reflections.ReflectionUtils;
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
            type = ReflectionUtils.forName(descriptor);
        }
        catch (Throwable t) {
            //return null and swallow the exception
            return null;
        }
        descriptorTypeMappings.put(descriptor, type);
        return type;
    }

    /**
     * Get a list of unique elements on the classpath as File objects, preserving order.
     * Classpath elements that do not exist are not returned.
     *
     * Uses the ResourceService to resolve classpath URLs. The "file" and "jar" protocols are
     * supported by default. Other protocols, for example "vfs", can be handled by writing
     * an appropriate resolver and registering it with the ServiceLoader mechanism
     *
     * @param classPaths classpaths to be included
     * @return {@link List} of unique {@link File} objects on the classpath
     */
    public static Set<File> getUniqueClasspathElements(List<String> classPaths) {
        Set<File> pathFiles = new HashSet<>();
        for(String classPath : classPaths) {
        }
        return pathFiles;
    }


}
