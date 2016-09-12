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

import org.neo4j.ogm.context.MappedRelationship;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vince
 */
public class RelationshipRegister {

    private final Set<MappedRelationship> register = Collections.newSetFromMap(new ConcurrentHashMap<MappedRelationship, Boolean>());

    public boolean contains(MappedRelationship relationship) {
        return register.contains(relationship);
    }

    public Set<MappedRelationship> values() {
        return Collections.unmodifiableSet(register);
    }

    public void add(MappedRelationship relationship) {
        register.add(relationship);
    }

    public void clear() {
        register.clear();
    }

    public boolean remove(MappedRelationship mappedRelationship) {
        return register.remove(mappedRelationship);
    }
}
