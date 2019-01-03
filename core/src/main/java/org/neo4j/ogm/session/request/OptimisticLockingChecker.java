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
package org.neo4j.ogm.session.request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.neo4j.ogm.exception.OptimisticLockingException;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.session.Neo4jSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frantisek Hartman
 */
public class OptimisticLockingChecker {

    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockingChecker.class);

    private final Neo4jSession session;

    public OptimisticLockingChecker(Neo4jSession session) {
        this.session = session;
    }

    public void checkResultsCount(List<RowModel> models, Statement request) {
        OptimisticLockingConfig olConfig = request.optimisticLockingConfig().orElseThrow(
            () -> new IllegalArgumentException("Statement " + request + " doesn't require results count check")
        );

        StringBuilder message = new StringBuilder();

        if (olConfig.getExpectedResultsCount() != models.size()) {
            message.append("Optimistic locking exception failed. ");

            Map<String, Object> parameters = request.getParameters();
            Object type = parameters.get("type");

            List<Map<String, Object>> rows = (List<Map<String, Object>>) parameters.get("rows");
            if (rows != null) {

                Map<Long, Long> nodeIds = new HashMap<>();
                Map<Long, Long> relIds = new HashMap<>();

                for (Map<String, Object> row : rows) {
                    if (type.equals("node")) {
                        nodeIds.put((Long) row.get("nodeId"), (Long) row.get(olConfig.getVersionProperty()));
                    } else if (type.equals("rel")) {
                        relIds.put((Long) row.get("relId"), (Long) row.get(olConfig.getVersionProperty()));
                    }
                }

                if (!models.isEmpty()) {

                    int idPosition = ArrayUtils.indexOf(models.get(0).variables(), "id");
                    for (RowModel model : models) {
                        Object id = model.getValues()[idPosition];
                        if (type.equals("node")) {
                            nodeIds.remove(id);
                        } else if (type.equals("rel")) {
                            relIds.remove(id);
                        }
                    }
                }

                for (Map.Entry<Long, Long> node : nodeIds.entrySet()) {
                    Long nodeId = node.getKey();
                    session.context().detachNodeEntity(nodeId);
                    message.append("Entity with type='").append(Arrays.toString(olConfig.getTypes())).append("' and id='")
                        .append(nodeId).append("' had incorrect version ").append(node.getValue());
                }

                for (Map.Entry<Long, Long> rel : relIds.entrySet()) {
                    Long relId = rel.getKey();
                    session.context().detachRelationshipEntity(relId);
                    message.append("Entity with id")
                        .append(relId).append("had incorrect version ").append(rel.getValue());
                }


            } else {
                Object id = parameters.get("id");
                if (id != null && models.isEmpty()) {

                    if (type.equals("node")) {
                        session.context().detachNodeEntity((Long) id);
                    } else if (type.equals("rel")) {
                        session.context().detachRelationshipEntity((Long) id);
                    }

                }
            }


            throw new OptimisticLockingException(message.toString());
        }
    }
}
