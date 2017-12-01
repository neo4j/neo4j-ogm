/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
package org.neo4j.ogm.typeconversion;

/**
 * Proxy implementation of {@link AttributeConverter} backed by an index to which custom generic converters
 * can be added after the object-graph mapping framework has been initialised.
 *
 * @author Adam George
 */
public final class ProxyAttributeConverter implements AttributeConverter<Object, Object> {

    private final ConversionCallbackRegistry converterCallbackRegistry;
    private final Class<?> entityAttributeType;
    private final Class<?> targetGraphType;

    /**
     * Constructs a new {@link ProxyAttributeConverter} based on the given arguments.
     *
     * @param entityAttributeType       The type of the attribute in the entity to convert
     * @param targetGraphType           The target type to which the value from the entity should be converted for saving into the graph
     * @param converterCallbackRegistry The {@link ConversionCallbackRegistry} from which to look up the converters
     */
    public ProxyAttributeConverter(Class<?> entityAttributeType, Class<?> targetGraphType,
        ConversionCallbackRegistry converterCallbackRegistry) {
        this.entityAttributeType = entityAttributeType;
        this.targetGraphType = targetGraphType;
        this.converterCallbackRegistry = converterCallbackRegistry;
    }

    @Override
    public Object toEntityAttribute(Object valueFromGraph) {
        ConversionCallback conversionCallback = this.converterCallbackRegistry.lookUpConverter();
        return conversionCallback.convert(this.entityAttributeType, valueFromGraph);
    }

    @Override
    public Object toGraphProperty(Object valueFromEntity) {
        ConversionCallback conversionCallback = this.converterCallbackRegistry.lookUpConverter();
        return conversionCallback.convert(this.targetGraphType, valueFromEntity);
    }
}
