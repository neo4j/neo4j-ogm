
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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.statement.parser.Clause;
import org.neo4j.ogm.cypher.statement.parser.MatchClause;
import org.neo4j.ogm.cypher.statement.parser.ReturnClause;
import org.neo4j.ogm.cypher.statement.parser.WithClause;

/**
 * @author Rene Richter
 */
public class ClauseTest {

    @Test
    public void itShouldBeAbleToCreateAReturnClause() throws Exception {
        ReturnClause returnClause = Clause.newReturnClause("RETURN a");
        assertTrue(returnClause != null);
    }

    @Test
    public void itShouldBeAbleToCreateAMatchClause() throws Exception {
        MatchClause matchClause = Clause.newMatchClause("MATCH (a)-[r:HAS_MANY]->(b)");
        assertTrue(matchClause != null);
    }

    @Test
    public void itShouldBeAbleToCreateAWithClause() throws Exception {
        WithClause matchClause = Clause.newWithClause("WITH distinct a,b");
        assertTrue(matchClause != null);
    }
}