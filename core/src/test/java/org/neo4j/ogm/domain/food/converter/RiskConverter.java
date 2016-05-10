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
package org.neo4j.ogm.domain.food.converter;

import org.neo4j.ogm.domain.food.entities.scanned.Risk;
import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Mihai Raulea
 * @author Luanne Misquitta
 */
public class RiskConverter implements AttributeConverter<Risk, String> {

	@Override
	public String toGraphProperty(org.neo4j.ogm.domain.food.entities.scanned.Risk value) {
		if (value == null) {
			return null;
		}
		return value.name();
	}

	@Override
	public Risk toEntityAttribute(String value) {
		if (value == null) {
			return null;
		}
		return Risk.valueOf(value);
	}
}
