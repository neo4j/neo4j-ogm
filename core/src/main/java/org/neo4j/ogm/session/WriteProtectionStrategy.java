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
package org.neo4j.ogm.session;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.neo4j.ogm.context.WriteProtectionTarget;

/**
 * A strategy that provides write protection for nodes in certain modes.
 * <br>
 * The strategy has to supply a function from traget and class to a predicate for objects of the given class. Thus predicates
 * can be distinguished by target and a set of certain object types and in the end, for instances itself.
 * <br>
 * The default implementation does not take the classes for objects into accounts but supplies to same predicate for all objects
 * for a given target.
 *
 * @author Michael J. Simons
 */
public interface WriteProtectionStrategy
    extends Supplier<BiFunction<WriteProtectionTarget, Class<?>, Predicate<Object>>> {
}
