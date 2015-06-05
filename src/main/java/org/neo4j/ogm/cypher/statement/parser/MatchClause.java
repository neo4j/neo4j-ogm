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

package org.neo4j.ogm.cypher.statement.parser;

import static org.neo4j.ogm.cypher.statement.parser.ParseUtils.lookFor;

import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Rene Richter
 */
public class MatchClause extends Clause {

    private static final Pattern extractMatchAliasesPattern = Pattern.compile(
            "(?<=\\()\\s*\\w+\\s*(?=(:|\\)))"       //Match aliases for nodes
          + "|"
          + "(?<=\\[)\\s*\\w+\\s*(?=(:|\\]))"       //Match aliases for relationships
          + "|"
          + "(?<=\\>|-)\\s*\\w+\\s*(?=($|\\z|-|<))" //Match aliases after > or -
          + "|"
          + "(?<=MATCH|=)\\s*\\w+\\s*(?=($|\\z|-|<))" //Match aliases after MATCH-keyword or equals-sign.
            ,Pattern.CASE_INSENSITIVE);

    private static final Pattern extractPathAliasPattern = Pattern.compile(
            "\\s*\\w+\\s*(?==)"  //Match everything that is followed by an = sign.
            ,Pattern.CASE_INSENSITIVE);


    private boolean matchesPath;

    public MatchClause(String content) {
        super(content);
    }

    /**
     * Indicates whether or not this MatchClause contains a path alias.
     * @return true, if it contains a path alias.
     */
    public boolean isPathMatch() {
        return matchesPath;
    }

    @Override
    public List<String> parseAliases() {
        List<String> aliases = lookFor(extractMatchAliasesPattern, getContent());
        List<String> pathAliases = lookFor(extractPathAliasPattern,getContent());
        this.matchesPath = !pathAliases.isEmpty();
        aliases.addAll(pathAliases);
        return aliases;
    }
}
