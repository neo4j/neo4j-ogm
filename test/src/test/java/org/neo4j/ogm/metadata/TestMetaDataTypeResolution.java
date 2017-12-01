/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import org.neo4j.ogm.utils.ClassUtils;

/**
 * @author vince
 */

public class TestMetaDataTypeResolution {

    private MetaData metaData = new MetaData("org.neo4j.ogm.metadata");

    public void checkField(String name, String expectedDescriptor, Class expectedPersistableType) {
        ClassInfo classInfo = metaData.classInfo("POJO");
        FieldInfo fieldInfo = classInfo.fieldsInfo().get(name);
        String fieldDescriptor = fieldInfo.getTypeDescriptor();
        assertThat(fieldDescriptor).isEqualTo(expectedDescriptor);
        Class clazz = ClassUtils.getType(fieldDescriptor);
        assertThat(clazz).isEqualTo(expectedPersistableType);
    }
}
