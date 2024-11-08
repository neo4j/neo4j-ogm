/*
 * Copyright (c) 2002-2024 "Neo4j,"
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
package org.neo4j.ogm.typeconversion;

import java.lang.reflect.Field;

/**
 * Converters registered via {@link org.neo4j.ogm.annotation.typeconversion.Convert @Convert} can require this information
 * via a non-default, public constructor taking in one single {@link ConverterInfo argument}.
 *
 * @param field               The field on which the converter was registered
 * @param fieldType           The field type that OGM does assume, might be different of what the raw field would give you
 * @param defaultPropertyName The property that would be assumed for this field by OGM
 */
public record ConverterInfo(Field field, Class<?> fieldType, String defaultPropertyName) {
}
