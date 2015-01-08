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
 * replaced with the correct node ids, and the a new MappedRelationship
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
