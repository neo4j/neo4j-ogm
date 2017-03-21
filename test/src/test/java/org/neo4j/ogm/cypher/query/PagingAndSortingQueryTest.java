/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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


package org.neo4j.ogm.cypher.query;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Jasper Blues
 */
public class PagingAndSortingQueryTest {

    @Test
    public void shouldAppendRelationshipIdentifiersCorrectly() {
        String cypher = "MATCH (n:`User`) WHERE n.`name` = { `name_0` } " +
                "MATCH (n)-[r0:`RATED`]-(m0) WHERE r0.`stars` = { `ratings_stars_1` } " +
                "WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)";

        Map<String, Object> params = new HashMap<>();
        params.put("name_0", "Jasper");
        params.put("ratings_stars_1", 1000);

        PagingAndSortingQuery query = new DefaultGraphModelRequest(cypher, params);
        query.setPagination(new Pagination(0, 4));

        String stmt = query.getStatement();
        assertEquals("MATCH (n:`User`) WHERE n.`name` = { `name_0` } " +
                "MATCH (n)-[r0:`RATED`]-(m0) WHERE r0.`stars` = { `ratings_stars_1` } " +
                "WITH n,r0 SKIP 0 LIMIT 4 MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", stmt);
        System.out.println(stmt);
    }
}
