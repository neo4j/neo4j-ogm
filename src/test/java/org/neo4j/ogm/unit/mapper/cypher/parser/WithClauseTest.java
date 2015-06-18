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

package org.neo4j.ogm.unit.mapper.cypher.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.neo4j.ogm.cypher.statement.parser.WithClause;

import java.util.List;

/**
 * Created by rene on 06.06.15.
 *
 * @author rene
 * @version 0.0.1
 */
public class WithClauseTest {

    WithClause instance;


    @Test
    public void test1() {
        instance = new WithClause("WITH h,i,j,k");
        List<String> aliases = instance.parseAliases();
        assertEquals(4,aliases.size());
        assertEquals("h",aliases.get(0));
        assertEquals("i",aliases.get(1));
        assertEquals("j",aliases.get(2));
        assertEquals("k",aliases.get(3));

    }

    @Test
    public void test2() {
        instance = new WithClause("WITH h  ,  i  , j , k  ");
        List<String> aliases = instance.parseAliases();
        assertEquals(4,aliases.size());
        assertEquals("h",aliases.get(0));
        assertEquals("i",aliases.get(1));
        assertEquals("j",aliases.get(2));
        assertEquals("k",aliases.get(3));

    }

    @Test
    public void test3() {
        instance = new WithClause("WITH h.hugo AS blubb ,i,j,k");
        List<String> aliases = instance.parseAliases();
        assertEquals(4,aliases.size());
        assertEquals("blubb",aliases.get(0));
        assertEquals("i",aliases.get(1));
        assertEquals("j",aliases.get(2));
        assertEquals("k",aliases.get(3));

    }

    @Test
    public void test4() {
        instance = new WithClause("WITH (h.hugo - 3*b) AS blubb ,i,j,k");
        List<String> aliases = instance.parseAliases();
        assertEquals(4,aliases.size());
        assertEquals("blubb",aliases.get(0));
        assertEquals("i",aliases.get(1));
        assertEquals("j",aliases.get(2));
        assertEquals("k",aliases.get(3));
    }

    @Test
    public void test5() {
        instance = new WithClause("WITH [a,b] as col UNWIND col as u");
        List<String> aliases = instance.parseAliases();
        assertEquals(1,aliases.size());
        assertEquals("u",aliases.get(0));
    }

    @Test
    public void test6() {
        instance = new WithClause("WITH sum(h.hugo - 3*b) AS blubb ,i,j,k");
        List<String> aliases = instance.parseAliases();
        assertEquals(4,aliases.size());
        assertEquals("blubb",aliases.get(0));
        assertEquals("i",aliases.get(1));
        assertEquals("j",aliases.get(2));
        assertEquals("k",aliases.get(3));
    }

    @Test
    public void test7() {
        instance = new WithClause("WITH sum(h.hugo - {modifier}*b.salery) AS blubb ,distinct i,j,k");
        List<String> aliases = instance.parseAliases();
        assertEquals(4,aliases.size());
        assertEquals("blubb",aliases.get(0));
        assertEquals("i",aliases.get(1));
        assertEquals("j",aliases.get(2));
        assertEquals("k",aliases.get(3));
    }
}
