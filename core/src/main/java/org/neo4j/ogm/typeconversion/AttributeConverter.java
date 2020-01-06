/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

/**
 * based on JPA AttributeConverter, but with methods
 * appropriate for property graphs, rather than
 * column stores/RDBMS.
 *
 * @param <T> the class of the entity attribute
 * @param <F> the class of the associated graph property
 * @author Vince Bickers
 */
public interface AttributeConverter<T, F> {

    F toGraphProperty(T value);

    T toEntityAttribute(F value);
}
