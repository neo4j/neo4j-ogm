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

package org.neo4j.ogm.context.register;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.MetaData;

/**
 * The TypeRegister maintains the list of active objects ids in the session, mapping each object id to its type hierarchy.
 * Thus a domain object with id 5 of type Person extends Entity will have 2 entries in the type register, one
 * in the Person map, and one in the Entity map.
 *
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class TypeRegister {

    private final Map<Class<?>, Map<Object, Object>> register = new HashMap<>();

    /**
     * Finds the map associated with an entity's class and removes the entity's id from the map (if found)
     *
     * @param metaData the domain model metadata
     * @param type the class of the entity to be removed
     * @param id the id of the entity to be removed
     */
    public void remove(MetaData metaData, Class type, Object id) {

        Map<Object, Object> entities = register.get(type);

        if (entities != null) {
            if (type.getSuperclass() != null && metaData != null && metaData.classInfo(type.getSuperclass().getName()) != null && !type.getSuperclass().getName().equals("java.lang.Object")) {
                entities.remove(id);
                remove(metaData, type.getSuperclass(), id);
            }
        }
    }

    /**
     * Returns an immutable map of the objects associated with the given type
     *
     * @param type the class whose map entries we want to return
     * @return the map's entries
     */
    public Map<Object, Object> get(Class<?> type) {
        return Collections.unmodifiableMap(objectMap(type));
    }

    /**
     * Removes all entries from the TypeRegister
     */
    public void clear() {
        register.clear();
    }

    /**
     * Finds the map associated with an entity's class and adds the id and entity to the map
     *
     * @param metaData the domain model metadata
     * @param type the class of the entity to be added
     * @param entity the entity to be added
     * @param id the id of the entity to be added
     */
    public void add(MetaData metaData, Class type, Object entity, Object id) {
        objectMap(type).put(id, entity);
        if (type.getSuperclass() != null
                && metaData != null
                && metaData.classInfo(type.getSuperclass().getName()) != null
                && !type.getSuperclass().getName().equals("java.lang.Object")) {
            add(metaData, type.getSuperclass(), entity, id);
        }
        if (type.getInterfaces() != null
                && metaData != null) {
            for (Class interfaceClass : type.getInterfaces()) {
                if (metaData.classInfo(interfaceClass.getName()) != null) {
                    add(metaData, interfaceClass, entity, id);
                }
            }
        }
    }

    /**
     * Removes the type from the register's keyset
     *
     * @param type the type to be removed
     */
    public void delete(Class<?> type) {
        register.keySet().remove(type);
    }

    private Map<Object, Object> objectMap(Class<?> type) {
        return register.computeIfAbsent(type, k -> new HashMap<>());
    }
}
