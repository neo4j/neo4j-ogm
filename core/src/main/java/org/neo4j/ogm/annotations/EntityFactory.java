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

package org.neo4j.ogm.annotations;


import org.neo4j.ogm.utils.MetaData;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.exception.BaseClassNotFoundException;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A metadata-driven factory class for creating node and relationship entities.
 *
 * @author Adam George
 */
public class EntityFactory {

    private final Map<String, String> taxaLeafClass = new HashMap<>();

    private final MetaData metadata;

    /**
     * Constructs a new {@link EntityFactory} driven by the specified {@link MetaData}.
     *
     * @param metadata The mapping {@link MetaData}
     */
    public EntityFactory(MetaData metadata) {
        this.metadata = metadata;
    }

    /**
     * Constructs a new object based on the class mapped to the labels on the given {@link Node}.  In the
     * case of multiple labels, only the one that identifies a class in the domain will be used, and if there
     * are any ambiguities in which label to use then an exception will be thrown.
     *
     * @param <T> The class of object to return
     * @param nodeModel The {@link Node} from which to determine the type
     * @return A new instance of the class that corresponds to the node label, never <code>null</code>
     * @throws MappingException if it's not possible to resolve or instantiate a class from the given argument
     */
    public <T> T newObject(Node nodeModel) {
        return instantiateObjectFromTaxa(nodeModel.getLabels());
    }

    /**
     * Constructs a new object based on the class mapped to the type in the given {@link org.neo4j.ogm.model.Edge}.
     *
     * @param <T> The class of object to return
     * @param edgeModel The {@link org.neo4j.ogm.model.Edge} from which to determine the type
     * @return A new instance of the class that corresponds to the relationship type, never <code>null</code>
     * @throws MappingException if it's not possible to resolve or instantiate a class from the given argument
     */
    public <T> T newObject(Edge edgeModel) {
        return instantiateObjectFromTaxa(edgeModel.getType());
    }

    /**
     * Constructs a new object based on the {@link ClassInfo}.
     *
     * @param <T> The class of object to return
     * @param classInfo The {@link ClassInfo} from which to determine the type
     * @return A new instance of the class that corresponds to the classinfo type, never <code>null</code>
     * @throws MappingException if it's not possible to resolve or instantiate a class from the given argument
     */
    public <T> T newObject(ClassInfo classInfo) {
        return (T) instantiate(classInfo.getUnderlyingClass());
    }

    /**
     * Constructs a new instance of the specified class using the same logic as the graph model factory methods.
     *
     * @param <T> The class of object to return
     * @param clarse The class to instantiate
     * @return A new instance of the specified {@link Class}
     * @throws MappingException if it's not possible to instantiate the given class for any reason
     */
    public <T> T newObject(Class<T> clarse) {
        return instantiate(clarse);
    }

    private <T> T instantiateObjectFromTaxa(String... taxa) {
        if (taxa == null || taxa.length == 0) {
            throw new BaseClassNotFoundException("<null>");
        }

        String fqn = resolve(taxa);

        try {
            @SuppressWarnings("unchecked")
            Class<T> loadedClass = (Class<T>) MetaDataClassLoader.loadClass(fqn); //Class.forName(fqn);
            return instantiate(loadedClass);
        } catch (ClassNotFoundException e) {
            throw new MappingException("Unable to load class with FQN: " + fqn, e);
        }
    }

    private String resolve(String... taxa) {

        String fqn = taxaLeafClass.get(Arrays.toString(taxa));

        if (fqn == null) {
            ClassInfo classInfo = metadata.resolve(taxa);
            if (classInfo != null) {
                taxaLeafClass.put(Arrays.toString(taxa), fqn=classInfo.name());
            } else {
                throw new BaseClassNotFoundException(Arrays.toString(taxa));
            }
        }
        return fqn;
    }

    private static <T> T instantiate(Class<T> loadedClass) {
        try {
            Constructor<T> defaultConstructor = loadedClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
        } catch (SecurityException | IllegalArgumentException | ReflectiveOperationException e) {
            throw new MappingException("Unable to instantiate " + loadedClass, e);
        }
    }

}
