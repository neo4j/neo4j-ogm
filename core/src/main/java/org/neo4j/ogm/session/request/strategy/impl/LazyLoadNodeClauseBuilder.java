/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import java.util.Collection;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.schema.Node;
import org.neo4j.ogm.metadata.schema.Relationship;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;

/**
 * @author Andreas Berger
 */
public class LazyLoadNodeClauseBuilder extends SchemaNodeLoadClauseBuilder implements LoadClauseBuilder {

    private MetaData metaData;

    public LazyLoadNodeClauseBuilder(MetaData metaData) {
        super(metaData.getSchema());
        this.metaData = metaData;
    }

    @Override
    protected Collection<Relationship> getRelevantRelationships(Node node) {
        return getNonLazyLoadingRelationships(metaData, node);
    }
}
