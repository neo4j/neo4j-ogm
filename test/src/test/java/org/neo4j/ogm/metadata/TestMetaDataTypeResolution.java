/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
