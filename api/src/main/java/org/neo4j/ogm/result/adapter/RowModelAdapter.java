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
package org.neo4j.ogm.result.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.support.CollectionUtils;

/**
 * This adapter will transform a Map&lt;String,Object&gt; typically representing an Embedded or Bolt response into a {@link RowModel} response
 *
 * @author vince
 * @author Luanne Misquitta
 */
public abstract class RowModelAdapter implements ResultAdapter<Map<String, Object>, RowModel> {

    private List<String> columns = new ArrayList<>();

    /**
     * Reads the next row from the result object and transforms it into a RowModel object
     *
     * @param data the data to transform, given as a map
     * @return the data transformed to an {@link RowModel}
     */
    public RowModel adapt(Map<String, Object> data) {

        if (columns == null) {
            throw new ResultProcessingException("Result columns should not be null");
        }

        // there is no guarantee that the objects in the data are ordered the same way as required by the columns
        // so we use the columns information to extract them in the correct order for post-processing.
        Iterator<String> iterator = columns.iterator();

        List<String> variables = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        while (iterator.hasNext()) {

            String key = iterator.next();
            Object value = data.get(key);

            if (isPath(value) || isNode(value) || isRelationship(value)) {
                continue;
            }
            variables.add(key);

            if (value != null && value.getClass().isArray()) {
                values.add(CollectionUtils.iterableOf(value));
            }

            values.add(value);
        }

        return new DefaultRowModel(values.toArray(new Object[] {}), variables.toArray(new String[] {}));
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public abstract boolean isPath(Object value);

    public abstract boolean isNode(Object value);

    public abstract boolean isRelationship(Object value);
}
