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
package org.neo4j.ogm.session.event;

/**
 * Represents a PersistenceEvent in the OGM.
 *
 * @author Vince Bickers
 */
public class PersistenceEvent implements Event {

    private Object affectedObject;
    private TYPE type;

    public PersistenceEvent(Object affectedObject, TYPE lifecycle) {
        this.affectedObject = affectedObject;
        this.type = lifecycle;
    }

    public Object getObject() {
        return affectedObject;
    }

    public TYPE getLifeCycle() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Event: %s, %s", type, affectedObject);
    }
}
