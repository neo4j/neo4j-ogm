package org.neo4j.ogm.session;

/**
 * Contains a minimal snapshot of relationships that were mapped from the database during a particular session.  The idea is
 * to help discern which relationships have been removed during the course of the session and subsequently issue appropriate
 * delete queries.
 */
public interface MappedRelationshipCache extends Iterable<MappedRelationship> {

    // no additional methods at the moment, although we'll probably need some eventually

}
