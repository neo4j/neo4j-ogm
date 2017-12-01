/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.domain.convertible.parametrized;

import static org.neo4j.driver.internal.util.Iterables.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Luanne Misquitta
 */
public class StringMapConverter implements AttributeConverter<Map<String, String>, List<String>> {

    @Override
    public List<String> toGraphProperty(Map<String, String> value) {
        return Arrays.asList("a", "b", "c");
    }

    @Override
    public Map<String, String> toEntityAttribute(List<String> value) {
        return map("a", "1", "b", "2", "c", "3");
    }
}
