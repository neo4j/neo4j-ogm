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

package org.neo4j.ogm.session.request;

import java.util.Map;

/**
 * @author vince
 */
public class FilteredQuery {

	private final StringBuilder stringBuilder;
	private final Map<String, Object> parameters;

	FilteredQuery(StringBuilder stringBuilder, Map<String, Object> parameters) {
		this.stringBuilder = stringBuilder;
		this.parameters = parameters;
	}

	public void setReturnClause(String returnClause) {
		stringBuilder.append(returnClause);
	}

	public String statement() {
		return stringBuilder.toString();
	}

	public  Map<String, Object> parameters() {
		return parameters;
	}

}

