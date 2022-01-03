/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private static final Pattern CHARS_TO_ESCAPE = Pattern.compile("([{}()\\[\\]^$?.+\\\\|!])");

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
