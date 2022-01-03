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
package org.neo4j.ogm.response.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;

/**
 * @author vince
 */
public class DefaultGraphRowListModel implements GraphRowListModel {

    private final List<GraphRowModel> model = new ArrayList<>();

    @Override
    public List<GraphRowModel> model() {
        return model;
    }

    public void add(GraphRowModel graphRowModel) {
        model.add(graphRowModel);
    }

    public void addAll(List<DefaultGraphRowModel> graphRowModels) {
        model.addAll(graphRowModels);
    }
}
