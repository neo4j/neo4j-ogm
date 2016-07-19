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

package org.neo4j.ogm.compiler.builders;

import java.util.Collection;

import org.neo4j.ogm.compiler.NodeBuilder;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.PropertyModel;

/**
 * @author Luanne Misquitta
 */
public class DefaultNodeBuilder implements NodeBuilder {

    NodeModel node = new NodeModel();


    public DefaultNodeBuilder(Long reference) {
        node.setId(reference);
    }

    @Override
    public NodeBuilder addProperty(String key, Object value) {
        node.getPropertyList().add(new PropertyModel<>(key, value));
        return this;
    }

    @Override
    public NodeBuilder addLabels(Collection<String> newLabels) {
        String[] labels;
        labels = newLabels.toArray(new String[newLabels.size()]);
        node.setLabels(labels);
        return this;
    }

    @Override
    public Long reference() {
        return node.getId();
    }

    @Override
    public String[] addedLabels() {
        return node.getLabels();
    }

    @Override
    public NodeBuilder removeLabels(Collection<String> removedLabels) {
        String[] labels;
        labels = removedLabels.toArray(new String[removedLabels.size()]);
        node.removeLabels(labels);
        return this;
    }

    @Override
    public String[] removedLabels() {
        return node.getRemovedLabels();
    }

    @Override
    public Node node() {
        return node;
    }
}
