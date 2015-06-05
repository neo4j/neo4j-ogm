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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neo4j.ogm.cypher.statement.parser.MatchClause;

import java.util.List;

/**
 * @author Rene Richter
 */
public class MatchClauseTest {


    private MatchClause instance;

    @Test
    public void test() {
        this.instance = new MatchClause("MATCH (a:User)-[pr:POSTS]->(p:Post)");
        List<String> aliases = instance.parseAliases();
        assertEquals(3,aliases.size());
        assertEquals("a",aliases.get(0));
    }

    @Test
    public void test2() {
        this.instance = new MatchClause("MATCH (a)-[:CAN_CODE]->(b)");
        List<String> aliases = instance.parseAliases();
        assertEquals(2,aliases.size());
        assertEquals("a",aliases.get(0));
    }

    @Test
    public void test3() {
        this.instance = new MatchClause("MATCH a-->(b)");
        List<String> aliases = instance.parseAliases();
        assertEquals(2,aliases.size());
        assertEquals("a",aliases.iterator().next());
    }

    @Test
    public void test4() {
        this.instance = new MatchClause("MATCH av<--b");
        List<String> aliases = instance.parseAliases();
        assertEquals(2,aliases.size());
        assertEquals("av",aliases.iterator().next());
    }

    @Test
    public void test5() {
        this.instance = new MatchClause("MATCH av  <-- b");
        List<String> aliases = instance.parseAliases();
        assertEquals(2,aliases.size());
        assertEquals("av",aliases.iterator().next());
    }

    @Test
    public void test6() {
        this.instance = new MatchClause("MATCH av<--b->c<--(e:Blubb)<-[bla:Hugo]");
        List<String> aliases = instance.parseAliases();
        assertEquals(5,aliases.size());
        assertEquals("av",aliases.iterator().next());
        assertEquals("bla",aliases.get(aliases.size()-1));
    }


    @Test
    public void itShouldBeTolerantToWhitSpaces() {
        this.instance = new MatchClause("MATCH av <-- b -> c <--( e :Blubb)<-[ bla :Hugo]");
        List<String> aliases = instance.parseAliases();
        assertEquals(5,aliases.size());
        assertEquals("av",aliases.get(0));
        assertEquals("bla",aliases.get(aliases.size()-1));
    }

    @Test
    public void itShouldAddPathAliasesLast() {
        this.instance = new MatchClause("MATCH     y = av <-- b -> c <--( e :Blubb)<-[ bla :Hugo]");
        List<String> aliases = instance.parseAliases();
        assertEquals(6,aliases.size());
        assertEquals("av",aliases.get(0));
        assertEquals("y",aliases.get(aliases.size()-1));
    }

    @Test
    public void itShouldIndicateThatItIsAPathMatchClause() {
        this.instance = new MatchClause("MATCH     y = av <-- b -> c <--( e :Blubb)<-[ bla :Hugo]");
        List<String> aliases = instance.parseAliases();
        assertEquals(6,aliases.size());
        assertEquals("av",aliases.get(0));
        assertEquals("y",aliases.get(aliases.size()-1));
        assertTrue(instance.isPathMatch());
    }



}
