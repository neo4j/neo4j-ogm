/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.cypher;

/**
 * {@link PropertyValueTransformer} that does nothing but pass through the property value.
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
