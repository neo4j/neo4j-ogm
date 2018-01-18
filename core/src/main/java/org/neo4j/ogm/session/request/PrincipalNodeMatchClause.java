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


import org.neo4j.ogm.cypher.Filter;

/**
 * @author Jasper Blues
 */
public class PrincipalNodeMatchClause implements MatchClause {

	private String label;

	private StringBuilder clause;

	public PrincipalNodeMatchClause(String label) {
		this.label = label;

		clause = new StringBuilder();
		clause.append(String.format("MATCH (n%s) ", this.label));
	}

	@Override
	public MatchClause append(Filter filter) {
		clause.append(filter.toCypher("n", clause.indexOf(" WHERE ") == -1));
		return this;
	}

	@Override
	public String toCypher() {
		return clause.toString();
	}
}
