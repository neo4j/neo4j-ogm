/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.context;

import java.util.Collection;
import java.util.HashSet;

/**
 * Tracks changes of labels to allow removing unused labels from the database node, when they're removed from an
 * entity in OGM.
 */
public class LabelHistory {

    private Collection<String> currentValues;
    private Collection<String> previousValues;

    public void push(Collection<String> values) {
        this.previousValues = new HashSet<>();
        if (values != null) {
            previousValues.addAll(values);
        }
        currentValues = values;
    }

    public Collection<String> getCurrentValues() {
        return currentValues;
    }

    public Collection<String> getPreviousValues() {
        return previousValues;
    }
}
