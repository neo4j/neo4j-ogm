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

package org.neo4j.ogm.metadata.schema;

import static java.util.Objects.*;
import static org.neo4j.ogm.annotation.Relationship.*;

/**
 * @author Frantisek Hartman
 */
class RelationshipImpl implements Relationship {

    private final String type;

    // Direction in regards to start and end node
    private final String direction;

    private final NodeImpl start;
    private final NodeImpl end;

    public RelationshipImpl(String type, String direction, NodeImpl start, NodeImpl end) {
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
    public String direction(Node node) {
        if (!(start == node || end == node)) {
            throw new IllegalArgumentException("Given node " + node +
                " is neiter start or end node of this relationship");
        } else if (start == end) {
            return direction;
        }

        switch (direction) {
            case UNDIRECTED:
                return UNDIRECTED;

            case OUTGOING:
                if (start == node) {
                    return OUTGOING;
                } else {
                    return INCOMING;
                }

            case INCOMING:
                if (end == node) {
                    return OUTGOING;
                } else {
                    return INCOMING;
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
                " is neiter start or end node of this relationship");
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
