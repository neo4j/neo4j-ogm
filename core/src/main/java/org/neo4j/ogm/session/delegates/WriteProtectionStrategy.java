/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.session.delegates;

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
