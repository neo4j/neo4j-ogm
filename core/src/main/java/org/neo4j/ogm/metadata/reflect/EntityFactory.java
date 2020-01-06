/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.exception.core.BaseClassNotFoundException;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.Node;

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
     * @param <T>       The class of object to return
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
     * @param <T>       The class of object to return
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
     * @param <T>       The class of object to return
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
     * @param <T>    The class of object to return
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

        @SuppressWarnings("unchecked")
        Class<T> loadedClass = (Class<T>) metadata.classInfo(fqn).getUnderlyingClass();
        return instantiate(loadedClass);
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
