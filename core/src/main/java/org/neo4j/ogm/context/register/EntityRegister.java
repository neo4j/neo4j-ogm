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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author vince
 */
public class EntityRegister  {

    private final ConcurrentMap<Long, Object> register = new ConcurrentHashMap<>();

    public Object get(Long id) {
        return register.get(id);
    }

    public boolean add(Long id, Object entity) {
        if (register.putIfAbsent(id, entity) == null) {
            return true;
        }
        return false;
    }

    public boolean contains(Long id) {
        return register.containsKey(id);
    }

    public void remove(Long id) {
        register.remove(id);
    }

    public void clear() {
        register.clear();
    }

    public Iterator<Long> iterator() {
        return register.keySet().iterator();
    }
}
