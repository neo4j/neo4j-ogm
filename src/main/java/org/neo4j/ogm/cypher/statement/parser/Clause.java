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


import java.util.ArrayList;
import java.util.List;

/**
 * Abstract representation of a cypher-query clause.
 *
 * @author Rene Richter
 */
public abstract class Clause  {

    private String content;
    private List<String> aliases = new ArrayList<>();

    protected Clause(String content) {
        this.content = content;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getContent() {
        return content;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    /**
     * Takes clause content and parses its aliases.
     * @return A list of aliases.
     */
    public abstract List<String> parseAliases();

    /**
     * Creates a new WithClause.
     *
     * The newly created WithClause gets parsed by calling the parseAliases method.
     * Use this method instead of the new operator.
     *
     * @param content The clause content.
     * @return A new WithClause instance.
     */
    public static WithClause newWithClause(String content) {
        WithClause clause = new WithClause(content);
        clause.setAliases(clause.parseAliases());
        return clause;
    }

    /**
     * Creates a new ReturnClause.
     *
     * The newly created ReturnClause gets parsed by calling the parseAliases method.
     * Use this method instead of the new operator.
     *
     * @param content The clause content.
     * @return A new ReturnClause instance.
     */
    public static ReturnClause newReturnClause(String content) {
        ReturnClause returnClause = new ReturnClause(content);
        returnClause.setAliases(returnClause.parseAliases());
        return returnClause;

    }

    /**
     * Creates a new MatchClause.
     *
     * The newly created MatchClause gets parsed by calling the parseAliases method.
     * Use this method instead of the new operator.
     *
     * @param content The clause content.
     * @return A new MatchClause instance.
     */
    public static MatchClause newMatchClause(String content) {
        MatchClause matchClause = new MatchClause(content);
        matchClause.setAliases(matchClause.parseAliases());
        return matchClause;
    }
}
