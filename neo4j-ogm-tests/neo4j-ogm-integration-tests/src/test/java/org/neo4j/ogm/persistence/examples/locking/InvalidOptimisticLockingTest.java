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
package org.neo4j.ogm.persistence.examples.locking;

import org.junit.Test;
import org.neo4j.ogm.domain.lockinginvalid.MultipleVersionFields;
import org.neo4j.ogm.exception.core.MetadataException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 */
public class InvalidOptimisticLockingTest extends TestContainersTestBase {

    @Test(expected = MetadataException.class)
    public void multipleVersionFields() {

        SessionFactory sf = new SessionFactory(getDriver(), MultipleVersionFields.class.getName());
        Session session = sf.openSession();

        session.save(new MultipleVersionFields());
    }
}
