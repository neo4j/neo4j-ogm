/*
 * Copyright (c)  [2011-2015] "Neo Technology"
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and licence terms.  Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's licence, as noted in the LICENSE file.
 */

package org.neo4j.ogm.cypher;

/**
 * Implementation of {@link PropertyValueTransformer} that transforms a traditional "LIKE" expression with asterisk wildcards
 * into a case-insensitive regular expression compatible with Cypher.
 *
 * @author Adam George
 */
public class CaseInsensitiveLikePropertyValueTransformer implements PropertyValueTransformer {

    @Override
    public Object transformPropertyValue(Object propertyValue) {
        return propertyValue != null ? "(?i)" + propertyValue.toString().replaceAll("\\*", ".*") : null;
    }

}
