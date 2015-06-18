/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.domain.convertible.date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Luanne Misquitta
 */
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String>{

	@Override
	public String toGraphProperty(LocalDateTime value) {
		if(value ==null) {
			return null;
		}
		return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	@Override
	public LocalDateTime toEntityAttribute(String value) {
		return LocalDateTime.parse(value,DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}
