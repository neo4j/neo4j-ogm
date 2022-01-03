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
package org.neo4j.ogm.utils

// A couple of extension methods that escapes $parameterNames inside multiline strings.
// See ParameterTest.kt for an example how to use them.

/**
 * Extension on [String] returning the string itself prefixed with an escaped `$`.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
inline fun String.asParam() = "\$" + this

/**
 * Extension on [String]'s companion object returning the string passed to it prefixed with an escaped `$`.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
infix fun String.Companion.asParam(s: String) = "\$" + s
