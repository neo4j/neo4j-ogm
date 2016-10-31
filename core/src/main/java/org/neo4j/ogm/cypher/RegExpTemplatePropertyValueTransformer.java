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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementation of {@link PropertyValueTransformer} that inserts the property value into a regular expression
 * template. If the property value contains regular expression instruction characters, they are escaped.
 *
 * @author Adam George
 */
public class RegExpTemplatePropertyValueTransformer implements PropertyValueTransformer {

    private String template;
    private Map<String, String> replacements;

    public RegExpTemplatePropertyValueTransformer(String template) {
        this.template = template;
        this.replacements = new HashMap<>();
    }

    // NB: wildcard character * is absent
    private static final Pattern CHARS_TO_ESCAPE = Pattern.compile("([{}\\(\\)\\[\\]^$?.+\\\\|!])");

    @Override
    public Object transformPropertyValue(Object propertyValue) {
        if (propertyValue != null) {
            String applied = String.format(template, applyReplacements(escapeRegexCharacters(propertyValue.toString())));
            return applied;
        }
        return null;
    }

    public RegExpTemplatePropertyValueTransformer replaceAll(String occurences, String with) {
        replacements.put(occurences, with);
        return this;
    }

    private static String escapeRegexCharacters(String propertyValue) {
        String replaced = CHARS_TO_ESCAPE.matcher(propertyValue).replaceAll("\\\\$1");
        return replaced;
    }

    private String applyReplacements(String value) {
        for (String key : replacements.keySet()) {
            value = value.replaceAll(key, replacements.get(key));
        }
        return value;
    }
}