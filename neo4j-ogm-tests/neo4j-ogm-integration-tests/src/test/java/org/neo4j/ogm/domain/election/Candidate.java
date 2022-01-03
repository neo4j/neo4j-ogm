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
package org.neo4j.ogm.domain.election;

/**
 * A Candidate is a voter and therefore may theoretically vote for herself.
 * <p/>
 * This will result in a loop edge in the graph
 *
 * @author vince
 * @See DATAGRAPH-689
 */
public class Candidate extends Voter {

    public Candidate() {
        super();
    }

    public Candidate(String s) {
        super(s);
    }
}
