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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Rene Richter
 */
public class ReturnClause  extends  Clause {

    private static final Pattern extractReturnAliasesPattern = Pattern.compile(
            "\\s*[\\w\\.]+\\b\\s*(?=(\\)|,|\\.|\\z|$))" //everything that is a word followed by either ".",")", ",", "end of line" or "end of string"
            ,Pattern.CASE_INSENSITIVE);

    public ReturnClause(String content) {
        super(content);
    }

    @Override
    public List<String> parseAliases() {
        List<String> aliases = new LinkedList<>();
        String dirtyAliasesString =  lookFor(extractReturnAliasesPattern, getContent()).get(0);
        for(String alias : lookFor(extractReturnAliasesPattern, getContent())) {
            //in some cases, the aliases have also properties. Eg: a.firstname.
            aliases.add(alias.split("\\.")[0]);
        }
        return aliases;
    }
}
