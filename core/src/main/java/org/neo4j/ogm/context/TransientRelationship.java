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
package org.neo4j.ogm.context;

import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.cypher.compiler.RelationshipBuilder;

/**
 * A TransientRelationship represents a relationship that is not yet
 * established in the graph, where at least one of either the
 * start node or end node is also a new object.
 * Transient Relationships are recorded while the cypher request
 * to save the domain model is being being constructed, and they are saved
 * in the log of the transaction's current context for post-processing
 * after the save request completes.
 * If the save succeeds, the ids of the two ends of the actual relationship
 * will now be fully known in the response. The start and end nodes of the transient
 * relationship (which were previously place holders) can now be
 * replaced with the correct node ids, and the new MappedRelationship
 * established in the session's mappingContext.
 *
 * @author Mark Angrish
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class TransientRelationship {

    private final Long src;
    private final Long tgt;
    private final Long ref;
    private final String rel;
    private final Class srcClass;
    private final Class tgtClass;

    public TransientRelationship(Long src, Long ref, String rel, Long tgt, Class srcClass, Class tgtClass) {
        this.src = src;
        this.tgt = tgt;
        this.ref = ref;
        this.rel = rel;
        this.srcClass = srcClass;
        this.tgtClass = tgtClass;
    }

    public boolean equals(Long otherSrc, RelationshipBuilder builder, Long otherTgt) {
        Boolean singleton = builder.isSingleton();
        if (this.rel.equals(builder.type())) {
            if (singleton) {
                if (builder.hasDirection(Direction.OUTGOING)) {
                    if (this.src.equals(otherSrc) && this.tgt.equals(otherTgt)) {
                        return true;
                    }
                } else if (builder.hasDirection(Direction.INCOMING)) {
                    if (this.src.equals(otherTgt) && this.tgt.equals(otherSrc)) {
                        return true;
                    }
                } else {
                    // Implies outgoing so the direction is ignored
                    if (this.src.equals(otherSrc) && this.tgt.equals(otherTgt)) {
                        return true;
                    }
                    if (this.src.equals(otherTgt) && this.tgt.equals(otherSrc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Long getSrc() {
        return src;
    }

    public Long getTgt() {
        return tgt;
    }

    public Long getRef() {
        return ref;
    }

    public String getRel() {
        return rel;
    }

    public Class getSrcClass() {
        return srcClass;
    }

    public Class getTgtClass() {
        return tgtClass;
    }

    public String toString() {
        return String.format("(%s)-[%s:%s]->(%s)", src, ref, rel, tgt);
    }
}
