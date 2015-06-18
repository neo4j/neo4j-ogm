/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.unit.mapper.cypher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.Query;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.query.SortOrder;

import java.util.HashMap;

/**
 *
 * @author Rene Richter
 */
public class CustomQueryPagingAndSortingTest {
    @Test
    public void itShouldTakeTheFirstReturnAliasForSorting() {
        String cypher = "match (u)->[a:HAS_MANY]->(t:Tupel{id:2}) RETURN u";
        Query query = new RowModelQuery(cypher,new HashMap<String,Object>());
        query.setPagination(new Pagination(0, 2));
        query.setSortOrder(new SortOrder().add("firstname").add("lastname"));

        check("match (u)->[a:HAS_MANY]->(t:Tupel{id:2}) WITH u,a,t ORDER BY u.firstname,u.lastname SKIP 0 LIMIT 2 RETURN u",query.getStatement());
    }

    @Test
    public void ifLastMatchContainsWithClauseItShouldNotGenerateANewWithClause() {
        String cypher = "match (u)->[a:HAS_MANY]->(t:Tupel{id:2}) WITH distinct u RETURN u";
        Query query = new RowModelQuery(cypher,new HashMap<String,Object>());
        query.setPagination(new Pagination(0, 2));
        query.setSortOrder(new SortOrder().add("firstname").add("lastname"));

        check("match (u)->[a:HAS_MANY]->(t:Tupel{id:2}) WITH distinct u ORDER BY u.firstname,u.lastname SKIP 0 LIMIT 2 RETURN u",query.getStatement());
    }


    @Test
    public void itShouldTakeRelationshipAliasIfFirstReturnAliasIsRelationshipAlias() {
        String cypher = "match ()->[a:HAS_MANY]->(t:Tupel{id:2}) RETURN a";
        Query query = new RowModelQuery(cypher,new HashMap<String,Object>());
        query.setPagination(new Pagination(0, 2));
        query.setSortOrder(new SortOrder().add("date"));
        check("match ()->[a:HAS_MANY]->(t:Tupel{id:2}) WITH a,t ORDER BY a.date SKIP 0 LIMIT 2 RETURN a",query.getStatement());
    }

    @Test
    public void sortingShouldGetResolvedByMatchIfLastMatchStatementContainsAPathAlias() {
        String cypher = "match p=()->[a:HAS_MANY]->(t:Tupel{id:2}) RETURN collect(distinct p)";
        Query query = new RowModelQuery(cypher,new HashMap<String,Object>());
        query.setPagination(new Pagination(0, 2));
        query.setSortOrder(new SortOrder().add("date"));
        check("match p=()->[a:HAS_MANY]->(t:Tupel{id:2}) WITH a,t,p ORDER BY a.date SKIP 0 LIMIT 2 RETURN collect(distinct p)",query.getStatement());
    }

    @Test
    public void ifLastMatchIsPathMatchAndHasAPreviousWithClauseTheWithClauseShouldResolvePagingAndSorting() {
        String cypher = "match a-->b with a,b match p=()->[a:HAS_MANY]->(t:Tupel{id:2}) RETURN collect(distinct p)";
        Query query = new RowModelQuery(cypher,new HashMap<String,Object>());
        query.setPagination(new Pagination(0, 2));
        query.setSortOrder(new SortOrder().add("date"));
        check("match a-->b with a,b ORDER BY a.date SKIP 0 LIMIT 2 match p=()->[a:HAS_MANY]->(t:Tupel{id:2}) RETURN collect(distinct p)",query.getStatement());
    }

    private void check(String expected, String actual) {
        assertEquals(expected, actual);
    }

}
