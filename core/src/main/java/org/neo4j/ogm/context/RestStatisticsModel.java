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
package org.neo4j.ogm.context;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.ogm.model.QueryStatistics;

/**
 * @author Luanne Misquitta
 */
public class RestStatisticsModel implements Iterable {

    Collection<Map<String, Object>> result;
    QueryStatistics statistics;

    public Collection<Map<String, Object>> getResult() {
        return result;
    }

    public void setResult(Collection<Map<String, Object>> result) {
        this.result = result;
    }

    public QueryStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(QueryStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public Iterator iterator() {
        return result.iterator();
    }
}
