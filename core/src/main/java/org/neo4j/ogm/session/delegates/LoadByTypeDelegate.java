/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.ogm.session.delegates;

import java.util.Collection;

import org.neo4j.ogm.utils.RelationshipUtils;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.context.GraphEntityMapper;
import org.neo4j.ogm.context.GraphRowListModelMapper;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.cypher.query.DefaultGraphRowListModelRequest;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortClause;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.request.GraphRowListModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class LoadByTypeDelegate implements Capability.LoadByType {

    private final Neo4jSession session;

    public LoadByTypeDelegate(Neo4jSession session) {
        this.session = session;
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination, int depth) {

        //session.ensureTransaction();
        String entityType = session.entityType(type.getName());
        QueryStatements queryStatements = session.queryStatementsFor(type);
        resolvePropertyAnnotations(type, sortOrder);

        // all this business about selecting which type of model/response to handle is horribly hacky
        // it should be possible for the response handler to select based on the model implementation
        // and we should have a single method loadAll(...). Filters should not be a special case
        // though they are at the moment because of the problems with "graph" response format.
        if (filters.isEmpty()) {

            PagingAndSortingQuery qry = queryStatements.findByType(entityType, depth)
                    .setSortOrder(sortOrder)
                    .setPagination(pagination);

            if (depth==0 || (pagination == null && sortOrder.toString().length() == 0)) { //if there is no sorting or paging or the depth=0, we don't want the row response back as well
                try (Response<GraphModel> response = session.requestHandler().execute((GraphModelRequest) qry)) {
                    return (Collection<T>) new GraphEntityMapper(session.metaData(), session.context()).map(type, response);
                }
            }
            else {
                DefaultGraphRowListModelRequest graphRowListModelRequest = new DefaultGraphRowListModelRequest(qry.getStatement(), qry.getParameters());
                try (Response<GraphRowListModel> response = session.requestHandler().execute(graphRowListModelRequest)) {
                    return (Collection<T>) new GraphRowListModelMapper(session.metaData(), session.context()).map(type, response);
                }
            }
        } else {

            filters = resolvePropertyAnnotations(type, filters);

            PagingAndSortingQuery qry = queryStatements.findByType(entityType, filters, depth)
                    .setSortOrder(sortOrder)
                    .setPagination(pagination);

            if (depth != 0) {
                try (Response<GraphRowListModel> response = session.requestHandler().execute((GraphRowListModelRequest) qry)) {
                    return (Collection<T>) new GraphRowListModelMapper(session.metaData(), session.context()).map(type, response);
                }
            } else {
                try (Response<GraphModel> response = session.requestHandler().execute((GraphModelRequest) qry)) {
                    return (Collection<T>) new GraphEntityMapper(session.metaData(), session.context()).map(type, response);
                }
            }
        }
    }


    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        return loadAll(type, new Filters(), new SortOrder(), null, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, int depth) {
        return loadAll(type, new Filters(), new SortOrder(), null, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter) {
        return loadAll(type, new Filters().add(filter), new SortOrder(), null, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, int depth) {
        return loadAll(type, new Filters().add(filter), new SortOrder(), null, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder) {
        return loadAll(type, new Filters().add(filter), sortOrder, null, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, int depth) {
        return loadAll(type, new Filters().add(filter), sortOrder, null, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination) {
        return loadAll(type, new Filters().add(filter), new SortOrder(), pagination, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination, int depth) {
        return loadAll(type, new Filters().add(filter), new SortOrder(), pagination, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination) {
        return loadAll(type, new Filters().add(filter), sortOrder, pagination, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination, int depth) {
        return loadAll(type, new Filters().add(filter), sortOrder, pagination, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters) {
        return loadAll(type, filters, new SortOrder(), null, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, int depth) {
        return loadAll(type, filters, new SortOrder(), null, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder) {
        return loadAll(type, filters, sortOrder, null, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, int depth) {
        return loadAll(type, filters, sortOrder, null, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination) {
        return loadAll(type, filters, new SortOrder(), pagination, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination, int depth) {
        return loadAll(type, filters, new SortOrder(), pagination, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination) {
        return loadAll(type, filters, sortOrder, pagination, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder) {
       return loadAll(type, new Filters(), sortOrder, null, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, int depth) {
        return loadAll(type, new Filters(), sortOrder, null, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Pagination paging) {
        return loadAll(type, new Filters(), new SortOrder(), paging, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Pagination paging, int depth) {
        return loadAll(type, new Filters(), new SortOrder(), paging, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination) {
        return loadAll(type, new Filters(), sortOrder, pagination, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination, int depth) {
        return loadAll(type, new Filters(), sortOrder, pagination, depth);
    }

    private Filters resolvePropertyAnnotations(Class entityType, Filters filters) {
        for(Filter filter : filters) {
            if(filter.getOwnerEntityType() == null) {
                filter.setOwnerEntityType(entityType);
            }
            filter.setPropertyName(resolvePropertyName(filter.getOwnerEntityType(), filter.getPropertyName()));
            if(filter.isNested()) {
                resolveRelationshipType(filter);
                ClassInfo nestedClassInfo = session.metaData().classInfo(filter.getNestedPropertyType().getName());
                filter.setNestedEntityTypeLabel(session.entityType(nestedClassInfo.name()));
                if(session.metaData().isRelationshipEntity(nestedClassInfo.name())) {
                    filter.setNestedRelationshipEntity(true);
                }
            }
        }
        return filters;
    }

    private void resolvePropertyAnnotations(Class entityType, SortOrder sortOrder) {
        final String escapedProperty = "`%s`";

        if (sortOrder != null) {
            for (SortClause sortClause : sortOrder.sortClauses()) {
                for (int i = 0; i < sortClause.getProperties().length; i++) {
                    sortClause.getProperties()[i] = String.format(escapedProperty, resolvePropertyName(entityType, sortClause.getProperties()[i]));
                }
            }
        }
    }

    private String resolvePropertyName(Class entityType, String propertyName) {
        ClassInfo classInfo = session.metaData().classInfo(entityType.getName());
        FieldInfo fieldInfo = classInfo.propertyFieldByName(propertyName);
        if (fieldInfo != null && fieldInfo.getAnnotations() != null) {
            AnnotationInfo annotation = fieldInfo.getAnnotations().get(Property.CLASS);
            if (annotation != null) {
                return annotation.get(Property.NAME, propertyName);
            }
        }
        return propertyName;
    }

    private void resolveRelationshipType(Filter parameter) {
        ClassInfo classInfo = session.metaData().classInfo(parameter.getOwnerEntityType().getName());
        FieldInfo fieldInfo = classInfo.relationshipFieldByName(parameter.getNestedPropertyName());

        String defaultRelationshipType = RelationshipUtils.inferRelationshipType(parameter.getNestedPropertyName());
        parameter.setRelationshipType(defaultRelationshipType);
        parameter.setRelationshipDirection(Relationship.UNDIRECTED);
        if (fieldInfo.getAnnotations() != null) {
            AnnotationInfo annotation = fieldInfo.getAnnotations().get(Relationship.CLASS);
            if (annotation != null) {
                parameter.setRelationshipType(annotation.get(Relationship.TYPE, defaultRelationshipType));
                parameter.setRelationshipDirection(annotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED));
            }
            if (fieldInfo.getAnnotations().get(StartNode.CLASS) != null) {
                parameter.setRelationshipDirection(Relationship.OUTGOING);
            }
            if (fieldInfo.getAnnotations().get(EndNode.CLASS) != null) {
                parameter.setRelationshipDirection(Relationship.INCOMING);
            }
        }
    }

}
