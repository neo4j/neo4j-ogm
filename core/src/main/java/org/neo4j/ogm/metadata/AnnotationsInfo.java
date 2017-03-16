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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vince Bickers
 */
public class AnnotationsInfo {

    private final Map<String, AnnotationInfo> classAnnotations;

    public AnnotationsInfo() {
        this.classAnnotations = new HashMap<>();
    }

    public AnnotationsInfo(Class<?> cls) {
        this.classAnnotations = new HashMap<>();

        final Annotation[] declaredAnnotations = cls.getDeclaredAnnotations();
        for (Annotation annotation: declaredAnnotations) {
            AnnotationInfo info = new AnnotationInfo(annotation);
            classAnnotations.put(info.getName(), info);
        }
    }

    public Collection<AnnotationInfo> list() {
        return classAnnotations.values();
    }

    /**
     * @param annotationName The fully-qualified class name of the annotation type
     * @return The {@link AnnotationInfo} that matches the given name or <code>null</code> if it's not present
     */
    public AnnotationInfo get(String annotationName) {
        return classAnnotations.get(annotationName);
    }

    public AnnotationInfo get(Class<?> annotationNameClass) {
        return classAnnotations.get(annotationNameClass.getName());
    }

    public void append(AnnotationsInfo annotationsInfo) {
        for (AnnotationInfo annotationInfo : annotationsInfo.list()) {
            classAnnotations.put(annotationInfo.getName(), annotationInfo);
        }
    }
}
