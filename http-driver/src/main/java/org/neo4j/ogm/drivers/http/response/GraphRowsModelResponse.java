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
package org.neo4j.ogm.drivers.http.response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultGraphRowListModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class GraphRowsModelResponse extends AbstractHttpResponse<DefaultGraphRowModel[]>
    implements Response<GraphRowListModel> {

    public GraphRowsModelResponse(CloseableHttpResponse httpResponse) {
        super(httpResponse, DefaultGraphRowModel[].class, false);
    }

    @Override
    public GraphRowListModel next() {
        GraphRowModel[] graphRowModel = nextDataRecord("data");

        if (graphRowModel != null) {
            DefaultGraphRowListModel graphRowListModel = new DefaultGraphRowListModel();
            for (GraphRowModel model : graphRowModel) {
                graphRowListModel.add(model);
            }
            return graphRowListModel;
        }
        return null;
    }
}
