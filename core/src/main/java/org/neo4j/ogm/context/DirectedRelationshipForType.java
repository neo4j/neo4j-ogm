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

package org.neo4j.ogm.context;

/**
 * A DirectedRelationship mapping to objects of a particular type.
 * @author Luanne Misquitta
 */
public class DirectedRelationshipForType extends DirectedRelationship {

	Class type;

	public DirectedRelationshipForType(String relationshipType, String relationshipDirection, Class type) {
		super(relationshipType, relationshipDirection);
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		DirectedRelationshipForType that = (DirectedRelationshipForType) o;

		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}
}
