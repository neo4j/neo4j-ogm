package org.neo4j.ogm.session.request.strategy.impl;

import org.neo4j.ogm.session.request.strategy.MatchClauseBuilder;

/**
 * @author Frantisek Hartman
 */
public class RelationshipTypeMatchClauseBuilder implements MatchClauseBuilder {

    @Override
    public String build(String label) {
        // we use r0 for historical reasons, e.g. in tests, but could easily be `r` or anything else
        return "MATCH ()-[r0:`" + label + "`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m";
    }

    @Override
    public String build(String label, String property) {
        throw new UnsupportedOperationException("MATCH by relationship type not supported with property parameter");
    }
}
