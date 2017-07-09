/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.session.request.strategy.impl;

import java.io.Serializable;
import java.util.Collection;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.DefaultGraphRowListModelRequest;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.exception.InvalidDepthException;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Luanne Misquitta
 */
public class RelationshipQueryStatements<ID extends Serializable> implements QueryStatements<ID> {

    private static final String MATCH_WITH_ID = "MATCH ()-[r0]-() WHERE ID(r0)={id} ";
    private static final String MATCH_WITH_TYPE_AND_ID = "MATCH ()-[r0:`%s`]-() WHERE ID(r0)={id} ";
    private static final String MATCH_WITH_IDS = "MATCH ()-[r0]-() WHERE ID(r0) IN {ids} ";
    private static final String MATCH_WITH_TYPE_AND_IDS = "MATCH ()-[r0:`%s`]-() WHERE ID(r0) IN {ids} ";
    private static final String MATCH_PATHS_WITH_REL_ID = " WITH r0,startnode(r0) AS n, endnode(r0) AS m " +
            "MATCH p1 = (n)-[*%d..%d]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*%d..%d]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH ID(r0) AS rId,startPaths + endPaths  AS paths " +
            "UNWIND paths AS p " +
            "RETURN DISTINCT p, rId";
    private static final String MATCH_PATHS = " WITH STARTNODE(r0) AS n, ENDNODE(r0) AS m " +
            "MATCH p1 = (n)-[*%d..%d]-() WITH COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*%d..%d]-() WITH startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH startPaths + endPaths AS paths " +
            "UNWIND paths AS p " +
            "RETURN DISTINCT p";

    @Override
    public PagingAndSortingQuery findOne(ID id, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry = String.format(MATCH_WITH_ID + MATCH_PATHS, min, max, min, max);
            return new PagingAndSortingQuery(qry, Utils.map("id", id));
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public PagingAndSortingQuery findOneByType(String label, ID id, int depth) {
        if (label == null || label.equals("")) {
            throw new IllegalArgumentException("no label provided");
        }

        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry = String.format(MATCH_WITH_TYPE_AND_ID + MATCH_PATHS, label, min, max, min, max);
            return new PagingAndSortingQuery(qry, Utils.map("id", id));
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public PagingAndSortingQuery findAllByType(String type, Collection<ID> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry = String.format(MATCH_WITH_TYPE_AND_IDS + MATCH_PATHS_WITH_REL_ID, type, min, max, max, max);
            return new PagingAndSortingQuery(qry, Utils.map("ids", ids));
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public PagingAndSortingQuery findByType(String type, int depth) {
        int max = max(depth);
        if (max > 0) {
            String qry = String.format("MATCH ()-[r0:`%s`]-() " + MATCH_PATHS_WITH_REL_ID, type, 0, max, 0, max);
            return new PagingAndSortingQuery(qry, Utils.map());
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }


    @Override
    public PagingAndSortingQuery findByType(String type, Filters parameters, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, parameters);
            query.setReturnClause(String.format(MATCH_PATHS_WITH_REL_ID, min, max, min, max));
            return new PagingAndSortingQuery(query.statement(), query.parameters());
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    private int min(int depth) {
        return Math.min(0, depth);
    }

    private int max(int depth) {
        return Math.max(0, depth);
    }
}
