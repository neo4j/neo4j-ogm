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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class AnnotationInfo {

    private String annotationName;
    private Map<String, String> elements;

    public AnnotationInfo(String annotationName, Map<String, String> elements) {
        this.annotationName = annotationName;
        this.elements = new HashMap<>(elements);
    }

    public String getName() {
        return annotationName;
    }

    public String get(String key, String defaultValue) {
        elements.putIfAbsent(key, defaultValue);
        return get(key);
    }

    public String get(String key) {
        return elements.get(key);
    }
}
