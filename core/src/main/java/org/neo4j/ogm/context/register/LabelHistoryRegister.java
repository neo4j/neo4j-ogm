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

import org.neo4j.ogm.context.LabelHistory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vince
 */
public class LabelHistoryRegister {

    //TODO: When CYPHER supports REMOVE ALL labels, we can stop tracking label changes
    private final ConcurrentHashMap<Long, LabelHistory> register = new ConcurrentHashMap<>();

    public void clear() {
        register.clear();
    }

    public LabelHistory get(Long identity) {
        register.putIfAbsent(identity, new LabelHistory());
        return register.get(identity);
    }
}
