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
