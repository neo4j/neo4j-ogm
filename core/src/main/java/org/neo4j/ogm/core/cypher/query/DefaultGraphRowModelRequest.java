/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.core.cypher.query;

import org.neo4j.ogm.api.request.GraphRowModelRequest;

import java.util.Map;

/**
 * A {@link org.neo4j.ogm.core.cypher.statement.CypherStatement} which returns data in both row and graph formats.
 *
 * @author Luanne Misquitta
 */
public class DefaultGraphRowModelRequest extends AbstractRequest implements GraphRowModelRequest {

    private final static String[] resultDataContents = new String[] {"graph", "row"};

	public DefaultGraphRowModelRequest(String cypher, Map<String, ?> parameters) {
		super(cypher, parameters);
	}

    // used by object mapper
    public String[] getResultDataContents() {
        return resultDataContents;
    }

}
