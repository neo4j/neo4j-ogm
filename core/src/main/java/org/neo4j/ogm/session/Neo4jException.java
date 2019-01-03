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
package org.neo4j.ogm.session;

/**
 * Top level Exception for Neo4j OGM.
 *
 * @author Mark Angrish
 *
 * @deprecated Since 3.1.6, will be removed in 3.2 Neo4j-OGM doesn't use this and there will be no replacement.
 */
@Deprecated
public class Neo4jException extends RuntimeException {

    public Neo4jException(String s) {
        super(s);
    }
}
