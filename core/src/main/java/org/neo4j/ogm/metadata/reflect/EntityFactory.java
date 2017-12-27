/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of the source code for these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 *
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
            if (classInfo != null) {
                taxaLeafClass.put(Arrays.toString(taxa), fqn = classInfo.name());
            } else {
                throw new BaseClassNotFoundException(Arrays.toString(taxa));
            }
        }
        return fqn;
    }

    private <T> T instantiate(Class<T> loadedClass, Map<String, Object> propertyValues) {
        return entityInstantiator.createInstance(loadedClass, propertyValues);
    }
}
