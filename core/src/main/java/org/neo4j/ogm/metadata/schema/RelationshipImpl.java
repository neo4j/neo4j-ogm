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
package org.neo4j.ogm.metadata.schema;

import static java.util.Objects.*;
import static org.neo4j.ogm.annotation.Relationship.*;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
class RelationshipImpl implements Relationship {

    private final String type;

    // Direction in regards to start and end node
    private final Direction direction;

    private final NodeImpl start;
    private final NodeImpl end;

    RelationshipImpl(String type, Direction direction, NodeImpl start, NodeImpl end) {
        this.type = requireNonNull(type);
        this.direction = requireNonNull(direction);
        this.start = requireNonNull(start);
        this.end = requireNonNull(end);
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Direction direction(Node node) {
        if (!(start == node || end == node)) {
            throw new IllegalArgumentException("Given node " + node +
                " is neither start or end node of this relationship");
        } else if (start == end) {
            return direction;
        }

        switch (direction) {
            case UNDIRECTED:
                return Direction.UNDIRECTED;

            case OUTGOING:
                if (start == node) {
                    return Direction.OUTGOING;
                } else {
                    return Direction.INCOMING;
                }

            case INCOMING:
                if (end == node) {
                    return Direction.OUTGOING;
                } else {
                    return Direction.INCOMING;
                }

            default:
                throw new IllegalStateException("Unknown direction " + direction);
        }
    }

    @Override
    public Node start() {
        return start;
    }

    @Override
    public Node other(Node node) {
        if (start == node) {
            return end;
        } else if (end == node) {
            return start;
        } else {
            throw new IllegalArgumentException("Given node " + node +
                " is neither start or end node of this relationship");
        }
    }

    @Override
    public String toString() {
        return "RelationshipImpl{" +
            "start=" + start +
            ", end=" + end +
            ", type='" + type + '\'' +
            ", direction='" + direction + '\'' +
            '}';
    }
}
