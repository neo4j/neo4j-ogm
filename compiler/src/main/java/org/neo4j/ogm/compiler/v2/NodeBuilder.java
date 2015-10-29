/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.compiler.v2;

import org.neo4j.ogm.compiler.NodeEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vince Bickers
 */
abstract class NodeBuilder implements NodeEmitter {

    private final String cypherReference;

    final Map<String, Object> props = new HashMap<>();
    final List<String> labels = new ArrayList<>();

    /**
     * Constructs a new {@link NodeBuilder} identified by the named variable in the context of its enclosing Cypher
     * query.
     *
     * @param variableName The name of the variable to use
     */
    NodeBuilder(String variableName) {
        this.cypherReference = variableName;
    }

    NodeBuilder addLabel(String labelName) {
        this.labels.add(labelName);
        return this;
    }

    @Override
    public NodeEmitter addProperty(String propertyName, Object value) {
        this.props.put(propertyName, value);
        return this;
    }

    public NodeEmitter addLabels(Iterable<String> labelName) {
        for (String label : labelName) {
            addLabel(label);
        }
        return this;
    }

    @Override
    public String toString() {
        return "(" + cypherReference + ":" + this.labels + " " + this.props + ")";
    }

    public static String toCsv(Iterable<String> elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            sb.append(element).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public String reference() {
        return cypherReference;
    }

    @Override
    public int compareTo(NodeEmitter o) {
        return cypherReference.compareTo(o.reference());
    }


}
