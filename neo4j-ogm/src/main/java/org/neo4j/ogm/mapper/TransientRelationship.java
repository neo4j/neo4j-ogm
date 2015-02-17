/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.mapper;

import java.util.Map;

/**
 * A TransientRelationship represents a relationship that is not yet
 * established in the graph, where at least one of either the
 * start node or end node is also a new object.
 *
 * Transient Relationships are recorded while the cypher request
 * to save the domain model is being being constructed, and they are saved
 * in the log of the transaction's current context for post-processing
 * after the save request completes.
 *
 * If the save succeeds, the ids of the two ends of the actual relationship
 * will now be fully known in the response. The start and end nodes of the transient
 * relationship (which were previously place holders) can now be
 * replaced with the correct node ids, and the new MappedRelationship
 * established in the session's mappingContext.
 *
 */
public class TransientRelationship {

    private final String src;
    private final String tgt;
    private final String rel;

    public TransientRelationship(String src, String rel, String tgt) {
        this.src = src;
        this.tgt = tgt;
        this.rel = rel;
    }

    /**
     * Creates a MappedRelationship from a TransientRelationship
     * using the supplied refMap to lookup and replace the correct start and end node ids
     * @param refMap A Map containing refs to the src/tgt ids
     * @return
     */
    public MappedRelationship convert(Map<String, Long> refMap) {

        Long srcIdentity = src.startsWith("_") ? refMap.get(src) : Long.parseLong(src.substring(1));
        Long tgtIdentity = tgt.startsWith("_") ? refMap.get(tgt) : Long.parseLong(tgt.substring(1));

        if (srcIdentity == null) {
            throw new RuntimeException("Couldn't get identity for " + src);
        }

        if (tgtIdentity == null) {
            throw new RuntimeException("Couldn't get identity for " + tgt);
        }

        return new MappedRelationship(srcIdentity, rel, tgtIdentity);
    }

    public boolean equalsIgnoreDirection(String src, String type, String tgt) {
        if (this.rel.equals(type)) {
            if (this.src.equals(src) && this.tgt.equals(tgt)) {
                return true;
            }
            if (this.src.equals(tgt) && this.tgt.equals(src)) {
                return true;
            }
        }
        return false;
    }
}
