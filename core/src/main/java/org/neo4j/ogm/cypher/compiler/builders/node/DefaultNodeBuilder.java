/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
