/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.cypher;

import java.util.regex.Pattern;

/**
 * Implementation of {@link PropertyValueTransformer} that transforms a traditional "LIKE" expression with asterisk wildcards
 * into a case-insensitive regular expression compatible with Cypher.
 *
 * @author Adam George
 */
public class CaseInsensitiveLikePropertyValueTransformer implements PropertyValueTransformer {

    // NB: wildcard character * is absent
    private static final Pattern CHARS_TO_ESCAPE = Pattern.compile("([{}\\(\\)\\[\\]^$?.+\\\\|!])");

    @Override
    public Object transformPropertyValue(Object propertyValue) {
        return propertyValue != null
                ? "(?i)" + escapeRegexCharacters(propertyValue.toString()).replaceAll("\\*", ".*")
                : null;
    }

    private static String escapeRegexCharacters(String propertyValue) {
        return CHARS_TO_ESCAPE.matcher(propertyValue).replaceAll("\\\\$1");
    }
}
