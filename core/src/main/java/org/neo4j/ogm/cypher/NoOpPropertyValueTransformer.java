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

package org.neo4j.ogm.cypher;

/**
 * {@link PropertyValueTransformer} that does nothing but pass through the property value.
 *
 * This is so that a {@link PropertyValueTransformer} never has to be set to <code>null</code> for a comparison operator if
 * no transformation is required.
 *
 * @author Adam George
 */
public class NoOpPropertyValueTransformer implements PropertyValueTransformer {

	@Override
	public Object transformPropertyValue(Object propertyValue) {
		return propertyValue;
	}

}