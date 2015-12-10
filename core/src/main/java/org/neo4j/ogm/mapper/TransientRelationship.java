/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.mapper;

import org.neo4j.ogm.compiler.RelationshipBuilder;

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

    public boolean equalsIgnoreDirection(Long src, RelationshipBuilder builder, Long tgt) {
        Boolean singleton = builder.isSingleton();
        if (this.rel.equals(builder.type())) {
            if (singleton) {
                if (this.src.equals(src) && this.tgt.equals(tgt)) {
                    return true;
                }
                if (this.src.equals(tgt) && this.tgt.equals(src)) {
                    return true;
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
}
