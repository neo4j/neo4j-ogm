/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.session.request.strategy.impl;

import org.neo4j.ogm.session.request.strategy.MatchClauseBuilder;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class LabelMatchClauseBuilder implements MatchClauseBuilder {

    @Override
    public String build(String label) {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Label to match is required.");
        }
        return "MATCH (n:`" + label + "`) WITH n";
    }

    @Override
    public String build(String label, String property) {
        throw new UnsupportedOperationException("MATCH by Label not supported with property parameter");
    }
}
