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
package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the primary unique constraint used to reference an EntityNode.
 * <p>When using an @Id on a class attribute, this attribute will be considered as the key of the entity,
 * and saving to the database will trigger a merge on an existing entry with the same key if it exists.
 * The @Id annotated attribute can either be assigned manually by the user (default), or can be generated
 * by OGM (see @{@link GeneratedValue}}.
 * <p>This comes as a more explicit replacement to the old {@link Index}(primary = true, unique = true) annotation.
 *
 * @author Mark Angrish
 * @since 3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Id {

}
