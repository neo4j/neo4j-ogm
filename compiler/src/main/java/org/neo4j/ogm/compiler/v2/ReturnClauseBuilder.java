/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.compiler.v2;

import org.neo4j.ogm.api.compiler.CypherEmitter;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Vince Bickers
 */
class ReturnClauseBuilder implements CypherEmitter {
    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        if (!varStack.isEmpty()) {
            queryBuilder.append(" RETURN ");
            for (Iterator<String> it = varStack.iterator(); it.hasNext(); ) {
                String var = it.next();
                queryBuilder.append("id(");
                queryBuilder.append(var);
                queryBuilder.append(") AS ");
                queryBuilder.append(var);
                if (it.hasNext()) {
                    queryBuilder.append(", ");
                }
            }
        }
        return !varStack.isEmpty();
    }
}
