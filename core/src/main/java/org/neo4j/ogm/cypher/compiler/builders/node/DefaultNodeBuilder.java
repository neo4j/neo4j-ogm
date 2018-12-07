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

package org.neo4j.ogm.cypher.compiler.builders.node;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.cypher.compiler.NodeBuilder;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.PropertyModel;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class DefaultNodeBuilder extends AbstractPropertyContainerBuilder<NodeBuilder, NodeModel> implements NodeBuilder {

    public DefaultNodeBuilder(Long reference) {
        super(new NodeModel(reference));
    }

    @Override
    public NodeBuilder addProperty(String key, Object value) {
        List<Property<String, Object>> propertyList = super.targetContainer.getPropertyList();

        for (Property<String, Object> property : propertyList) {
            if (property.getKey().equals(key)) {
                throw new MappingException("Node model already contains property: " + key);
            }
        }

        propertyList.add(new PropertyModel<>(key, value));
        return this;
    }

    @Override
    public NodeBuilder addLabels(Collection<String> newLabels) {
        String[] labels = newLabels.toArray(new String[newLabels.size()]);
        super.targetContainer.setLabels(labels);
        return this;
    }

    @Override
    public Long reference() {
        return super.targetContainer.getId();
    }

    @Override
    public NodeBuilder setPreviousDynamicLabels(Set<String> previousDynamicLabels) {
        super.targetContainer.setPreviousDynamicLabels(previousDynamicLabels);
        return this;
    }

    @Override
    public Node node() {
        return super.targetContainer;
    }

    @Override
    public NodeBuilder setPrimaryIndex(String primaryIndexField) {
        super.targetContainer.setPrimaryIndex(primaryIndexField);
        return this;
    }

    @Override
    public NodeBuilder setVersionProperty(String name, Long version) {
        super.targetContainer.setVersion(new PropertyModel<>(name, version));
        return this;
    }

}
