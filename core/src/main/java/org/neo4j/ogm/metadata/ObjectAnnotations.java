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

package org.neo4j.ogm.metadata;

import org.neo4j.ogm.annotation.typeconversion.*;
import org.neo4j.ogm.metadata.classloader.MetaDataClassLoader;
import org.neo4j.ogm.typeconversion.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vince Bickers
 */
public class ObjectAnnotations {

    private String objectName; // fully qualified class, method or field name.
    private final Map<String, AnnotationInfo> annotations = new HashMap<>();

    public void put(String key, AnnotationInfo value) {
        annotations.put(key, value);
    }

    public AnnotationInfo get(String key) {
        return annotations.get(key);
    }

    public AnnotationInfo get(Class<?> keyClass) {
        return annotations.get(keyClass.getCanonicalName());
    }

    public boolean isEmpty() {
        return annotations.isEmpty();
    }

    Object getConverter() {

        // try to get a custom type converter
        AnnotationInfo customType = get(Convert.class);
        if (customType != null) {
            String classDescriptor = customType.get(Convert.CONVERTER, null);
            if (classDescriptor == null) {
                return null; // will have a default proxy converter applied later on
            }

            try {
                String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
                Class<?> clazz = MetaDataClassLoader.loadClass(className);//Class.forName(className);
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // try to find a pre-registered type annotation. this is very clumsy, but at least it is done only once
        AnnotationInfo dateLongConverterInfo = get(DateLong.class);
        if (dateLongConverterInfo != null) {
            return new DateLongConverter();
        }

        AnnotationInfo dateStringConverterInfo = get(DateString.class);
        if (dateStringConverterInfo != null) {
            String format = dateStringConverterInfo.get(DateString.FORMAT, DateString.ISO_8601);
            return new DateStringConverter(format);
        }

        AnnotationInfo enumStringConverterInfo = get(EnumString.class);
        if (enumStringConverterInfo != null) {
            String classDescriptor = enumStringConverterInfo.get(EnumString.TYPE, null);
            String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
            try {
                Class clazz = MetaDataClassLoader.loadClass(className);//Class.forName(className);
                return new EnumStringConverter(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        AnnotationInfo numberStringConverterInfo = get(NumberString.class);
        if (numberStringConverterInfo != null) {
            String classDescriptor = numberStringConverterInfo.get(NumberString.TYPE, null);
            String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
            try {
                Class clazz = MetaDataClassLoader.loadClass(className);//Class.forName(className);
                return new NumberStringConverter(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

}
