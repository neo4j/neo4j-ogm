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
