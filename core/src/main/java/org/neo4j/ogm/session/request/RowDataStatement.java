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

package org.neo4j.ogm.session.request;

import java.util.Map;

import org.neo4j.ogm.request.Statement;

/**
 * @author Luanne Misquitta
 */
public class RowDataStatement implements Statement {

	private String statement;
	private Map<String, Object> parameters;
	private String[] resultDataContents = new String[] {"row"};

	public RowDataStatement() {
	}

	public RowDataStatement(String statement, Map<String, Object> parameters) {
		this.statement = statement;
		this.parameters = parameters;
	}

	@Override
	public String getStatement() {
		return statement;
	}

	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}

	@Override
	public String[] getResultDataContents() {
		return resultDataContents;
	}

	@Override
	public boolean isIncludeStats() {
		return false;
	}
}
