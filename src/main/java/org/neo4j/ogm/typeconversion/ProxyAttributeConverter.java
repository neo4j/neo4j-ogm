/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and licence terms.  Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's licence, as noted in the LICENSE file.
 */
package org.neo4j.ogm.typeconversion;

/**
 * Proxy implementation of {@link AttributeConverter} backed by an index to which custom generic converters
 * can be added after the object-graph mapping framework has been initialised.
 */
public final class ProxyAttributeConverter implements AttributeConverter<Object, Object> {

    private final ConversionCallbackRegistry converterCallbackRegistry;
    private final Class<?> entityAttributeType;

    public ProxyAttributeConverter(Class<?> entityAttributeType, ConversionCallbackRegistry converterCallbackRegistry) {
        this.entityAttributeType = entityAttributeType;
        this.converterCallbackRegistry = converterCallbackRegistry;
    }

    @Override
    public Object toEntityAttribute(Object valueFromGraph) {
        ConversionCallback conversionCallback = converterCallbackRegistry.lookUpConverter();
        return conversionCallback.convert(valueFromGraph.getClass(), entityAttributeType, valueFromGraph);
    }

    @Override
    public Object toGraphProperty(Object valueFromEntity) {
        ConversionCallback conversionCallback = converterCallbackRegistry.lookUpConverter();
        // FIXME: we need to determine the correct graph property target type for this to work
        return conversionCallback.convert(entityAttributeType, Number.class, valueFromEntity);
    }

}
