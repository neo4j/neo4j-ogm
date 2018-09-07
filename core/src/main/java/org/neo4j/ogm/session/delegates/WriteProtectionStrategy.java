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

import org.neo4j.ogm.context.WriteProtectionMode;

/**
 * A strategy that provides write protection for nodes in certain modes.
 *
 * @author Michael J. Simons
 */
public interface WriteProtectionStrategy extends Supplier<BiFunction<WriteProtectionMode, Object, Predicate<Object>>> {
}
