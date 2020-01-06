/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        for (Annotation annotation : declaredAnnotations) {
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
