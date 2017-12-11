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

package org.neo4j.ogm.cypher.query;

import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.RowModelRequest;

/**
 * @author Vince Bickers
 */
public class DefaultRowModelRequest extends CypherQuery implements RowModelRequest {

    private final static String[] resultDataContents = new String[] { "row" };
    private OptimisticLockingConfig optimisticLockingConfig;

    public DefaultRowModelRequest(String cypher, Map<String, ?> parameters) {
        this(cypher, parameters, null);
    }

    public DefaultRowModelRequest(String cypher, Map<String, ?> parameters,
        OptimisticLockingConfig optimisticLockingConfig) {

        super(cypher, parameters);
        this.optimisticLockingConfig = optimisticLockingConfig;
    }

    // used by object mapper
    public String[] getResultDataContents() {
        return resultDataContents;
    }

    @Override
    public Optional<OptimisticLockingConfig> optimisticLockingConfig() {
        return Optional.ofNullable(optimisticLockingConfig);
    }
}
