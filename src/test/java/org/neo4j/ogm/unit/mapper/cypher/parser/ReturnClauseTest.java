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
import org.neo4j.ogm.cypher.statement.parser.ReturnClause;

import java.util.List;

/**
 *
 * @author Rene Richter
 */
public class ReturnClauseTest {

    ReturnClause instance;

    @Test
    public void itShouldMatchSimpleAliases() {
        instance = new ReturnClause("RETURN a,b,c");
        List<String> aliases = instance.parseAliases();
        assertEquals(3,aliases.size());
        assertEquals("a",aliases.get(0));
    }

    @Test
    public void itShouldMatchAliasesAfterDots() {
        instance = new ReturnClause("RETURN collect(distinct uhu ),b,sum(collect(c.gurgl))");
        List<String> aliases = instance.parseAliases();
        assertEquals(3,aliases.size());
        assertEquals("uhu",aliases.get(0));
        assertEquals("b",aliases.get(1));
        assertEquals("c",aliases.get(2));
    }

    @Test
    public void itShouldBeAbleToExtractAliasesFromDeeplyNestedFunctions() {
        instance = new ReturnClause("RETURN distinct sum(collect(bla(blubb(a)))))");
        List<String> aliases = instance.parseAliases();
        assertEquals(1,aliases.size());
        assertEquals("a",aliases.get(0));
    }

    @Test
    public void itShouldBeTolerantToWhitspaces() {
        instance = new ReturnClause("RETURN distinct sum (   collect  (   bla  (  blubb  (  a  ) )   ) ) )");
        List<String> aliases = instance.parseAliases();
        assertEquals(1,aliases.size());
        assertEquals("a",aliases.get(0));
    }

    @Test
    public void itShouldIgnoreModifyingWords() {
        instance = new ReturnClause("return distinct a");
        List<String> aliases = instance.parseAliases();
        assertEquals(1,aliases.size());
        assertEquals("a",aliases.get(0));
    }

    @Test
    public void itShouldIgnoreModifyingWordsEvenIfNotYetDefined() {
        instance = new ReturnClause("return asdf a");
        List<String> aliases = instance.parseAliases();
        assertEquals(1,aliases.size());
        assertEquals("a",aliases.get(0));
    }

    @Test
    public void itShouldWorkWithArithmeticOperations() {
        instance = new ReturnClause("return sum(a.hallo - b.hugo)");
        List<String> aliases = instance.parseAliases();
        assertEquals(2,aliases.size());
        assertEquals("a",aliases.get(0));
        assertEquals("b",aliases.get(1));
    }




}
