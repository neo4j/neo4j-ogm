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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Rene Richter
 */
public class ParseUtils {

    //Helpermethod to collect matches.
    public static List<String> lookFor(Pattern pattern,String text) {
        Matcher m = pattern.matcher(text);
        List<String> returnValues = new LinkedList<>();
        while(m.find()) {
            returnValues.add(m.group().trim());
        }
        return returnValues;
    }
}
