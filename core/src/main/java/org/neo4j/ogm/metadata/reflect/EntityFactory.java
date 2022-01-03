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
package org.neo4j.ogm.metadata.reflect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.exception.core.BaseClassNotFoundException;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.session.EntityInstantiator;

/**
 * A metadata-driven factory class for creating node and relationship entities.
 *
 * @author Adam George
 * @author Nicolas Mervaillie
 */
public class EntityFactory {

    private final Map<String, String> taxaLeafClass = new HashMap<>();

    private final MetaData metadata;
    private EntityInstantiator entityInstantiator;

    /**
     * Compatibility constructor for SDN 5.0.
     *
     * @param metadata The mapping {@link MetaData}
     */
    public EntityFactory(MetaData metadata) {
        this.metadata = metadata;
        this.entityInstantiator = new ReflectionEntityInstantiator(metadata);
    }

    /**
     * Constructs a new {@link EntityFactory} driven by the specified {@link MetaData}.
     *
     * @param metadata The mapping {@link MetaData}
     * @param entityInstantiator The instantiation mechanism to be used.
     */
    public EntityFactory(MetaData metadata, EntityInstantiator entityInstantiator) {
        this.metadata = metadata;
        this.entityInstantiator = entityInstantiator;
    }

    /**
     * Constructs a new object based on the class mapped to the labels on the given {@link Node}.  In the
     * case of multiple labels, only the one that identifies a class in the domain will be used, and if there
     * are any ambiguities in which label to use then an exception will be thrown.
     *
     * @param <T>       The class of object to return
     * @param nodeModel The {@link Node} from which to determine the type
     * @return A new instance of the class that corresponds to the node label, never <code>null</code>
     * @throws MappingException if it's not possible to resolve or instantiate a class from the given argument
     */
    public <T> T newObject(Node nodeModel) {
        Map<String, Object> map = new HashMap<>();

        for (Property<String, Object> property : nodeModel.getPropertyList()) {
            map.put(property.getKey(), property.getValue());
        }
        return instantiateObjectFromTaxa(nodeModel.getLabels(), map);
    }

    /**
     * Constructs a new instance of the specified class using the same logic as the graph model factory methods.
     *
     * @param <T>    The class of object to return
     * @param clarse The class to instantiate
     * @return A new instance of the specified {@link Class}
     * @throws MappingException if it's not possible to instantiate the given class for any reason
     */
    public <T> T newObject(Class<T> clarse, Map<String, Object> map) {
        return instantiate(clarse, map);
    }

    private <T> T instantiateObjectFromTaxa(String[] taxa, Map<String, Object> propertyValues) {
        if (taxa == null || taxa.length == 0) {
            throw new BaseClassNotFoundException("<null>");
        }

        String fqn = resolve(taxa);

        @SuppressWarnings("unchecked")
        Class<T> loadedClass = (Class<T>) metadata.classInfo(fqn).getUnderlyingClass();
        return instantiate(loadedClass, propertyValues);
    }

    private String resolve(String... taxa) {

        String fqn = taxaLeafClass.get(Arrays.toString(taxa));

        if (fqn == null) {
            ClassInfo classInfo = metadata.resolve(taxa);
            if (classInfo == null) {
                throw new BaseClassNotFoundException(Arrays.toString(taxa));
            }
            fqn = classInfo.name();
            taxaLeafClass.put(Arrays.toString(taxa), fqn);
        }
        return fqn;
    }

    private <T> T instantiate(Class<T> loadedClass, Map<String, Object> propertyValues) {
        return entityInstantiator.createInstance(loadedClass, propertyValues);
    }
}
