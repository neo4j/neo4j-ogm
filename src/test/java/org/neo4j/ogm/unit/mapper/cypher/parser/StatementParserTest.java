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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neo4j.ogm.cypher.statement.parser.Clause;
import org.neo4j.ogm.cypher.statement.parser.MatchClause;
import org.neo4j.ogm.cypher.statement.parser.ReturnClause;
import org.neo4j.ogm.cypher.statement.parser.StatementParser;
import org.neo4j.ogm.cypher.statement.parser.WithClause;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author Rene Richter
 */
public class StatementParserTest {

    StatementParser parser = new StatementParser();

    @Test
    public void test1() {
        String query = "MATCH  (a:User{id:1}),(b:User{id:2}) WITH [a,b] as col UNWIND col as u ORDER BY u.firstname,u.lastname SKIP 10 LIMIT 30 RETURN u";
        LinkedList<Clause> clauses = parser.parseStatement(query);
        ListIterator<Clause> clauseListIterator = clauses.listIterator();
        assertTrue(clauseListIterator.next() instanceof MatchClause);
        assertTrue(clauseListIterator.next() instanceof WithClause);
        assertTrue(clauseListIterator.next() instanceof ReturnClause);
    }

    @Test
    public void test2() {
        String query = "MATCH a-->b WHERE a.age < 20 WITH a,b match p=c-[]->()<-[]-(b) WITH p RETURN collect(distinct p)";
        LinkedList<Clause> clauses = parser.parseStatement(query);
        ListIterator<Clause> clauseListIterator = clauses.listIterator();
        assertTrue(clauseListIterator.next() instanceof MatchClause);
        assertTrue(clauseListIterator.next() instanceof WithClause);
        assertTrue(clauseListIterator.next() instanceof MatchClause);
        assertTrue(clauseListIterator.next() instanceof WithClause);
        assertTrue(clauseListIterator.next() instanceof ReturnClause);
    }


}
