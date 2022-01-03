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
package org.neo4j.ogm.session.request.strategy.impl;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;
import org.neo4j.ogm.session.request.strategy.MatchClauseBuilder;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 */
public class NodeQueryStatements<ID extends Serializable> implements QueryStatements<ID> {

    /**
     * Some arbitrary characters that hopefully are not used in a {@link org.neo4j.ogm.annotation.Property @Property}.
     * We use them to separate the properties in composite primary keys when passing them through the existing API.
     * This constant is not to be used outside OGM.
     */
    public static final String PROPERTY_SEPARATOR = "4f392f2f-24b6-4f83-a474-b942a77cd01a";

    /**
     * This method concatenates the keys of an id into one string in case the id is a map, aka a composite key.
     * <p>
     * This method is the obverse of {@link #splitPrimaryIndexAttributes(String)}
     *
     * @param defaultPrimaryIndex The default primary index to use (the one applicable when the id is not a composite key)
     * @param id                  The actual id
     * @return The concatenated keys or the default, primary index to use
     * (the name of the property annotated with {@link org.neo4j.ogm.annotation.Id @Id}
     * or {@link org.neo4j.ogm.annotation.Index @Index}.
     */
    public static String joinPrimaryIndexAttributesIfNecessary(String defaultPrimaryIndex, Object id) {

        String primaryIndexToUse = defaultPrimaryIndex;
        if (id != null && id instanceof Map) {
            primaryIndexToUse = ((Map<String, Object>) id).keySet().stream()
                .collect(joining(PROPERTY_SEPARATOR));
        }
        return primaryIndexToUse;
    }

    /**
     * This joins properties separated by {@link #PROPERTY_SEPARATOR} into a literal map to be used to match on..
     *
     * @param property The property that may contain multiple, separated properties.
     * @return A literal map or a single property reference
     */
    public static String splitPrimaryIndexAttributes(String property) {

        if (property.contains(PROPERTY_SEPARATOR)) {
            return Arrays.stream(property.split(PROPERTY_SEPARATOR))
                .map(p -> "`" + p + "`: n.`" + p + "`")
                .collect(Collectors.joining(",", "{", "}"));
        } else {
            return "n.`" + property + "`";
        }
    }

    private final String primaryIndex;

    private final MatchClauseBuilder idMatchClauseBuilder = new IdMatchClauseBuilder();
    private final MatchClauseBuilder idCollectionMatchClauseBuilder = new IdCollectionMatchClauseBuilder();
    private final MatchClauseBuilder labelMatchClauseBuilder = new LabelMatchClauseBuilder();

    private final LoadClauseBuilder loadClauseBuilder;


    public NodeQueryStatements() {
        this(null, new PathNodeLoadClauseBuilder());
    }

    public NodeQueryStatements(String primaryIndex, LoadClauseBuilder loadClauseBuilder) {
        this.primaryIndex = primaryIndex;
        this.loadClauseBuilder = requireNonNull(loadClauseBuilder);
    }

    @Override
    public PagingAndSortingQuery findOne(ID id, int depth) {
        return findOneByType("", id, depth);
    }

    @Override
    public PagingAndSortingQuery findOneByType(String label, ID id, int depth) {

        String primaryIndexToUse = joinPrimaryIndexAttributesIfNecessary(primaryIndex, id);

        String matchClause;
        if (primaryIndex != null) {
            matchClause = idMatchClauseBuilder.build(label, primaryIndexToUse);
        } else {
            matchClause = idMatchClauseBuilder.build(label);
        }
        String returnClause = loadClauseBuilder.build(label, depth);
        return new PagingAndSortingQuery(matchClause, returnClause, Collections.singletonMap("id", id), depth != 0,
            false);
    }

    @Override
    public PagingAndSortingQuery findAllByType(String label, Collection<ID> ids, int depth) {

        // We assume maps with the same keys for all ids if this is a composite. Otherwise
        // this method would not make sense for a composite key.
        String primaryIndexToUse = joinPrimaryIndexAttributesIfNecessary(
            primaryIndex,
            ids.isEmpty() ? null : ids.iterator().next()
        );

        String matchClause;
        if (primaryIndex != null) {
            matchClause = idCollectionMatchClauseBuilder.build(label, primaryIndexToUse);
        } else {
            matchClause = idCollectionMatchClauseBuilder.build(label);
        }

        String returnClause = loadClauseBuilder.build(label, depth);
        return new PagingAndSortingQuery(
            matchClause, returnClause, Collections.singletonMap("ids", ids), depth != 0, false);
    }

    @Override
    public PagingAndSortingQuery findByType(String label, int depth) {
        String matchClause = labelMatchClauseBuilder.build(label);
        String returnClause = loadClauseBuilder.build(label, depth);
        return new PagingAndSortingQuery(matchClause, returnClause, Collections.emptyMap(), depth != 0, false);
    }

    @Override
    public PagingAndSortingQuery findByType(String label, Filters parameters, int depth) {
        FilteredQuery filteredQuery = FilteredQueryBuilder.buildNodeQuery(label, parameters);
        String matchClause = filteredQuery.statement();
        String returnClause = loadClauseBuilder.build(label, depth);
        return new PagingAndSortingQuery(matchClause, returnClause, filteredQuery.parameters(), depth != 0, true);
    }
}
