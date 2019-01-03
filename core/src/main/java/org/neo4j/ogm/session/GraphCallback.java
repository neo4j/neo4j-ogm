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
package org.neo4j.ogm.session;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;

/**
 * A callback interface used to pass functional code to the {@link Session} to be executed in a transaction
 * and with access to the underlying OGM meta-data.
 *
 * @param <T> The type of object returned from applying this callback
 * @author Adam George
 */
@Deprecated
public interface GraphCallback<T> {

    /**
     * Called by the OGM {@link Session} in a transaction to perform some arbitrary database operation.
     *
     * @param requestHandler The {@link org.neo4j.ogm.request.Request} for communication with the database
     * @param transaction    The {@link Transaction} in which the database communication is taking place
     * @param metaData       The mapping {@link MetaData} that pertains to the current session
     * @return An arbitrary result (or <code>null</code>) based on the desired behaviour of this callback function
     */
    T apply(Request requestHandler, Transaction transaction, MetaData metaData);
}
