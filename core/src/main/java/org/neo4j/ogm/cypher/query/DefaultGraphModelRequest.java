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

package org.neo4j.ogm.cypher.query;

import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.session.Utils;

import java.util.Map;

/**
 * @author Vince Bickers
 */
public class DefaultGraphModelRequest extends AbstractRequest implements GraphModelRequest {

    private final static String[] resultDataContents = new String[] {"graph"};

    public DefaultGraphModelRequest(Statement request) {
        this(request.getStatement(), request.getParameters());
    }

    public DefaultGraphModelRequest(String cypher) {
        this(cypher, Utils.map());
    }

    public DefaultGraphModelRequest(String cypher, Map<String, ?> parameters) {
        super(cypher, parameters);
    }

    // used by object mapper
    public String[] getResultDataContents() {
        return resultDataContents;
    }
}
