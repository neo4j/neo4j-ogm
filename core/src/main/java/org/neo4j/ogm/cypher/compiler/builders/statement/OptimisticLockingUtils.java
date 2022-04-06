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
package org.neo4j.ogm.cypher.compiler.builders.statement;

import org.neo4j.ogm.model.PropertyContainer;

/**
 * Provides fragments that implement optimistic locking logic via Cypher.
 * The main logic is as follows:
 * <ol>
 * <li>{@code AND n.`version` = {version}} Ensure that we only match the current version</li>
 * <li>{@code SET n.`version` = n.`version` + 1} Take the lock by incrementing the version of the record in this transaction</li>
 * <li>{@code WITH n WHERE n.version = {version} + 1} Add a second version check in case another transaction took the lock between our read and upgrade</li>
 * </ol>
 * For new nodes we take a merge into account and therefor check whether the record to upgrade has a version or not.
 * If it doesn't have a version yet, we start at 0.
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
final class OptimisticLockingUtils {

    private final static String VERSION_PROPERTY_CHECK_FOR_EXISTING_NODES_AND_RELATIONSHIPS = ""
        + "AND %1$s.`%2$s` = row.`%2$s` "
        + "SET %1$s.`%2$s` = %1$s.`%2$s` + %3$d "
        + "WITH %1$s, row "
        + "WHERE %1$s.`%2$s` = row.`%2$s` + %3$d ";

    /**
     * In case an entity with an externally assigned ID has also a @Version field, the version check is
     * slightly different than in the case of BaseBuilder.appendVersionPropertyCheck:
     * When the MERGE statement creates a new node, than the version passed to the statement as input
     * parameter is null and has to be treated accordingly (cannot compare null with null in the 1st where)
     * We also make sure that the first version will be 0.
     */
    private final static String VERSION_PROPERTY_CHECK_FOR_NEW_OR_EXISTING_NODES = ""
        + "WITH %1$s, row "
        + "WHERE (row.`%2$s` IS NULL AND %1$s.`%2$s` IS NULL) OR %1$s.`%2$s` = row.`%2$s` "
        + "  SET %1$s.`%2$s` = COALESCE(%1$s.`%2$s`, -1) + 1 "
        + "WITH %1$s, row "
        + "WHERE (row.`%2$s` IS NULL OR %1$s.`%2$s` = row.`%2$s` + 1) ";


    /**
     * @param container node / relationship to check
     * @param variable  The variable representing the node or relationship to upgrade
     * @return A fragment to be included in a statement to increment the version property
     */
    static String getFragmentForExistingNodesAndRelationships(PropertyContainer container, String variable) {
        return getFragmentForExistingNodesAndRelationships(container, variable, 1);
    }

    /**
     * @param container node / relationship to check
     * @param variable  The variable representing the node or relationship to upgrade
     * @param increment use {@literal 0} to avoid any increment (for non-dirty relationships)
     * @return A fragment to be included in a statement to increment the version property
     */
    static String getFragmentForExistingNodesAndRelationships(PropertyContainer container, String variable, int increment) {
        String key = container.getVersion().getKey();
        return String.format(VERSION_PROPERTY_CHECK_FOR_EXISTING_NODES_AND_RELATIONSHIPS, variable, key, increment);
    }

    /**
     * @param container node / relationship to check
     * @param variable  The variable representing the node or relationship to upgrade
     * @return
     */
    static String getFragmentForNewOrExistingNodes(PropertyContainer container, String variable) {
        String key = container.getVersion().getKey();
        return String.format(VERSION_PROPERTY_CHECK_FOR_NEW_OR_EXISTING_NODES, variable, key);
    }

    private OptimisticLockingUtils() {
    }
}
