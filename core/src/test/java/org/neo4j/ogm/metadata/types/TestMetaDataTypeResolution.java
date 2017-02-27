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

package org.neo4j.ogm.metadata.types;

import org.junit.Assert;
import org.neo4j.ogm.metadata.MetadataMap;
import org.neo4j.ogm.metadata.ClassMetadata;
import org.neo4j.ogm.metadata.FieldMetadata;
import org.neo4j.ogm.metadata.MethodMetadata;
import org.neo4j.ogm.utils.ClassUtils;

/**
 * @author vince
 */
public class TestMetaDataTypeResolution {

	private MetadataMap metaData = new MetadataMap("org.neo4j.ogm.metadata.types");

	protected void checkMethod(String name, String expectedDescriptor, Class expectedPersistableType) {
		ClassMetadata classInfo = metaData.classInfo("POJO");
		MethodMetadata methodInfo = classInfo.methodsInfo().get(name);
		String methodTypeDescriptor = methodInfo.getTypeDescriptor();
		Assert.assertEquals(expectedDescriptor, methodTypeDescriptor);
		Class clazz = ClassUtils.getType(methodTypeDescriptor);
		Assert.assertEquals(expectedPersistableType, clazz);
	}

	protected void checkField(String name, String expectedDescriptor, Class expectedPersistableType) {
		ClassMetadata classInfo = metaData.classInfo("POJO");
		FieldMetadata fieldInfo = classInfo.fieldsInfo().get(name);
		String fieldDescriptor = fieldInfo.getTypeDescriptor();
		Assert.assertEquals(expectedDescriptor, fieldDescriptor);
		Class clazz = ClassUtils.getType(fieldDescriptor);
		Assert.assertEquals(expectedPersistableType, clazz);
	}
}
