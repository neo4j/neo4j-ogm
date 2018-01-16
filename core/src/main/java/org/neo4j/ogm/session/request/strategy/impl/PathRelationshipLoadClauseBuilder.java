package org.neo4j.ogm.session.request.strategy.impl;

import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;

/**
 * Path based load clause builder for nodes - starts from given relationship variable
 *
 * @author Frantisek Hartman
 */
public class PathRelationshipLoadClauseBuilder implements LoadClauseBuilder {

    @Override
    public String build(String label, int depth) {
        return build("r", label, depth);
    }

    @Override
    public String build(String variable, String label, int depth) {
        return " MATCH p1 = (n)-[*0.." + depth + "]-() " +
            "WITH " + variable + ", COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*0.." + depth + "]-() " +
            "WITH " + variable + ", startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH " + variable + ",startPaths + endPaths  AS paths " +
            "UNWIND paths AS p " +
            "RETURN DISTINCT p";
    }
}
