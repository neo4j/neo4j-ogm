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
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session.request.strategy.impl;

import java.io.Serializable;
import java.util.Collection;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.DefaultGraphRowListModelRequest;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

import static org.neo4j.ogm.session.request.strategy.impl.TypesUtil.labelsToType;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Nicolas Mervaillie
 */
public class NodeQueryStatements<ID extends Serializable> implements QueryStatements<ID> {

    private String primaryIndex;

    public NodeQueryStatements() {
        // do nothing...
    }

    public NodeQueryStatements(String primaryIndex) {
        this.primaryIndex = primaryIndex;
    }

    @Override
    public PagingAndSortingQuery findOne(ID id, int depth) {
        return findOneByType("", id, depth);
    }


    @Override
    public PagingAndSortingQuery findOneByType(String label, ID id, int depth) {
        return findOneByType(singleton(label), id, depth);
    }

    @Override
    public PagingAndSortingQuery findOneByType(Collection<String> labels, ID id, int depth) {
        String type = labelsToType(labels);

        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findOne(id, primaryIndex);
        }
        if (max > 0) {
            String qry;
            if (primaryIndex != null) {
                qry = String.format("MATCH (n%s) WHERE n." + primaryIndex + " = { id } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", type, min, max);
            } else {
                qry = String.format("MATCH (n%s) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", type, min, max);
            }
            return new DefaultGraphModelRequest(qry, Utils.map("id", id));
        } else {
            return DepthZeroReadStrategy.findOne(id, primaryIndex);
        }
    }

    @Override
    public PagingAndSortingQuery findAll(Collection<ID> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findAll(ids);
        }
        if (max > 0) {
            String qry = String.format("MATCH (n) WHERE ID(n) IN { ids } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", min, max);
            return new DefaultGraphModelRequest(qry, Utils.map("ids", ids));
        } else {
            return DepthZeroReadStrategy.findAll(ids);
        }
    }

    @Override
    public PagingAndSortingQuery findAllByType(String label, Collection<ID> ids, int depth) {
        return findAllByType(singleton(label), ids, depth);
    }

    @Override
    public PagingAndSortingQuery findAllByType(Collection<String> types, Collection<ID> ids, int depth) {
        String label = labelsToType(types);
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findAllByLabel(label, ids, primaryIndex);
        }
        if (max > 0) {
            String qry;
            if (primaryIndex != null) {
                qry = String.format("MATCH (n%s) WHERE n." + primaryIndex + " IN { ids } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", label, min, max);
            } else {
                qry = String.format("MATCH (n%s) WHERE ID(n) IN { ids } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", label, min, max);
            }
            return new DefaultGraphModelRequest(qry, Utils.map("ids", ids));
        } else {
            return DepthZeroReadStrategy.findAllByLabel(label, ids, primaryIndex);
        }
    }

    @Override
    public PagingAndSortingQuery findAll() {
        return new DefaultGraphModelRequest("MATCH p=()-->() RETURN p", Utils.map());
    }

    @Override
    public PagingAndSortingQuery findByType(String label, int depth) {
        return findByType(singleton(label), depth);
    }

    @Override
    public PagingAndSortingQuery findByType(Collection<String> labels, int depth) {
        String label = labelsToType(labels);
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findByLabel(label);
        }
        if (max > 0) {
            String qry = String.format("MATCH (n%s) WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", label, min, max);
            return new DefaultGraphModelRequest(qry, Utils.map());
        } else {
            return DepthZeroReadStrategy.findByLabel(label);
        }
    }

    @Override
    public PagingAndSortingQuery findByType(String label, Filters parameters, int depth) {
        return findByType(singletonList(label), parameters, depth);
    }

    @Override
    public PagingAndSortingQuery findByType(Collection<String> labels, Filters parameters, int depth) {
        String label = labelsToType(labels);
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findByProperties(label, parameters);
        }
        if (max > 0) {
            FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, parameters);
            query.setReturnClause(String.format("WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p, ID(n)", min, max));
            return new DefaultGraphRowListModelRequest(query.statement(), query.parameters());
        } else {
            return DepthZeroReadStrategy.findByProperties(label, parameters);
        }
    }

    private int min(int depth) {
        return Math.min(0, depth);
    }

    private int max(int depth) {
        return Math.max(0, depth);
    }

    private static class DepthZeroReadStrategy {

        public static <ID extends Serializable> DefaultGraphModelRequest findOne(ID id, String primaryIndex) {
            if (primaryIndex != null) {
                return new DefaultGraphModelRequest("MATCH (n) WHERE n." + primaryIndex + " = { id } RETURN n", Utils.map("id", id));
            }
            return new DefaultGraphModelRequest("MATCH (n) WHERE ID(n) = { id } RETURN n", Utils.map("id", id));
        }

        public static <ID extends Serializable> DefaultGraphModelRequest findAll(Collection<ID> ids) {
            return new DefaultGraphModelRequest("MATCH (n) WHERE ID(n) IN { ids } RETURN n", Utils.map("ids", ids));
        }

        public static <ID extends Serializable> DefaultGraphModelRequest findAllByLabel(String label, Collection<ID> ids, String primaryIndex) {
            String queryString;
            if (primaryIndex != null) {
                queryString = String.format("MATCH (n%s) WHERE n." + primaryIndex + " IN { ids } RETURN n", label);
            } else {
                queryString = String.format("MATCH (n%s) WHERE ID(n) IN { ids } RETURN n", label);
            }
            return new DefaultGraphModelRequest(queryString, Utils.map("ids", ids));
        }


        public static DefaultGraphModelRequest findByLabel(String label) {
            return new DefaultGraphModelRequest(String.format("MATCH (n%s) RETURN n", label), Utils.map());
        }

        public static DefaultGraphModelRequest findByProperties(String label, Filters parameters) {
            FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, parameters);
            query.setReturnClause("RETURN n");
            return new DefaultGraphModelRequest(query.statement(), query.parameters());
        }
    }

    private static class InfiniteDepthReadStrategy {

        public static <ID extends Serializable> DefaultGraphModelRequest findOne(ID id, String primaryIndex) {
            if (primaryIndex != null) {
                return new DefaultGraphModelRequest("MATCH (n) WHERE n." + primaryIndex + " = { id } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", Utils.map("id", id));
            }
            return new DefaultGraphModelRequest("MATCH (n) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", Utils.map("id", id));
        }

        public static <ID extends Serializable> DefaultGraphModelRequest findAll(Collection<ID> ids) {
            return new DefaultGraphModelRequest("MATCH (n) WHERE ID(n) IN { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", Utils.map("ids", ids));
        }

        public static <ID extends Serializable> DefaultGraphModelRequest findAllByLabel(String label, Collection<ID> ids, String primaryIndex) {
            String queryString;
            if (primaryIndex != null) {
                queryString = String.format("MATCH (n%s) WHERE n." + primaryIndex + " IN { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", label);
            } else {
                queryString = String.format("MATCH (n%s) WHERE ID(n) IN { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", label);
            }
            return new DefaultGraphModelRequest(queryString, Utils.map("ids", ids));
        }

        public static DefaultGraphModelRequest findByLabel(String label) {
            return new DefaultGraphModelRequest(String.format("MATCH (n%s) WITH n MATCH p=(n)-[*0..]-(m) RETURN p", label), Utils.map());
        }

        public static DefaultGraphRowListModelRequest findByProperties(String label, Filters parameters) {
            FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, parameters);
            query.setReturnClause(" WITH n MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)");
            return new DefaultGraphRowListModelRequest(query.statement(), query.parameters());
        }
    }
}
