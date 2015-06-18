
/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.cypher.statement.parser;


import static org.neo4j.ogm.cypher.statement.parser.ParseUtils.lookFor;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * A parser for cypher queries to extract key-components of a query.
 * @author Rene Richter
 */
public class StatementParser {

    //precompiled pattern should boost performance.
    private static final Pattern extractClausesPattern = Pattern.compile(
            "(WITH.*?(?=($|MATCH|ORDER|RETURN|SKIP|LIMIT)))"
          + "|"
          + "(MATCH.*?(?=($|WHERE|RETURN|WITH)))"
          + "|"
          + "(RETURN.*?(\\z|$))"
            ,Pattern.CASE_INSENSITIVE);

    /**
     * Parses the statement and extracts MATCH,WITH and RETURN-clauses.
     * @param statement The statement that should get parsed.
     * @return A LinkedList containing all matched clauses.
     */
    public LinkedList<Clause> parseStatement(String statement) {
        LinkedList<Clause> result = new LinkedList<>();
        for (String clauseString : lookFor(extractClausesPattern, statement)) {
            clauseString = clauseString.trim();
            if(clauseString.toLowerCase().startsWith("with")) {
                result.add(Clause.newWithClause(clauseString));
            } else if (clauseString.toLowerCase().startsWith("match")) {
                result.add(Clause.newMatchClause(clauseString));
            } else {
                result.add(Clause.newReturnClause(clauseString));
            }
        }
        return  result;
    }
}
