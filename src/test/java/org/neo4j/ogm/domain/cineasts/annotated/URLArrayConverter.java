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
package org.neo4j.ogm.domain.cineasts.annotated;

import java.net.MalformedURLException;
import java.net.URL;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Luanne Misquitta
 */
public class URLArrayConverter implements AttributeConverter<URL[], String[]> {

	@Override
	public String[] toGraphProperty(URL[] value) {
		if (value == null) {
			return null;
		}

		String[] values = new String[value.length];
		for (int i=0; i < value.length; i++) {
			values[i] = value[i].toString();
		}
		return values;
	}

	@Override
	public URL[] toEntityAttribute(String[] value) {
		if (value == null) {
			return null;
		}
		URL[] urls = new URL[value.length];
		for (int i=0; i < value.length; i++) {
			try {
				urls[i] = new URL(value[i]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return urls;
	}
}
