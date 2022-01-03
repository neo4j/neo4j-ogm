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

import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.cypher.compiler.PropertyContainerBuilder;
import org.neo4j.ogm.model.PropertyContainer;

/**
 * This is a helper class for both {@link DefaultNodeBuilder} and {@link DefaultRelationshipBuilder} to take care of
 * composite properties (adding as well as tracking).
 *
 * @author Michael J. Simons
 * @soundtrack Led Zeppelin - Led Zeppelin IV
 */
abstract class AbstractPropertyContainerBuilder<SELF, P extends PropertyContainer>
    implements PropertyContainerBuilder<SELF> {

    protected P targetContainer;

    AbstractPropertyContainerBuilder(P targetContainer) {
        this.targetContainer = targetContainer;
    }

    @Override
    public final SELF addCompositeProperties(Map<String, ?> properties) {

        properties.forEach(this::addProperty);
        targetContainer.addCurrentDynamicCompositeProperties(properties.keySet());

        return self();
    }

    @Override
    public final SELF setPreviousCompositeProperties(Set<String> previousCompositeProperties) {
        targetContainer.setPreviousDynamicCompositeProperties(previousCompositeProperties);
        return self();
    }

    @SuppressWarnings("unchecked")
    final SELF self() {
        return (SELF) this;
    }
}
