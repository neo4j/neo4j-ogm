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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
@RunWith(MockitoJUnitRunner.class)
public class BookmarkTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Driver nativeDriver;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private org.neo4j.driver.v1.Session nativeSession;
    private Session session;

    @Before
    public void setUp() throws Exception {
        BoltDriver driver = new BoltDriver(nativeDriver);
        session = new Neo4jSession(new MetaData("org.neo4j.ogm.empty"), driver);

        when(nativeDriver.session(any(AccessMode.class), anyIterable())).thenReturn(nativeSession);
        when(nativeSession.beginTransaction().isOpen()).thenReturn(true);
        when(nativeSession.lastBookmark()).thenReturn("last-bookmark");
    }

    @Test
    public void shouldPassBookmarksToDriver() throws Exception {
        Set<String> bookmarks = new HashSet<>(Arrays.asList("bookmark1", "bookmark2"));

        Transaction transaction = session.beginTransaction(Transaction.Type.READ_ONLY, bookmarks);

        verify(nativeDriver).session(AccessMode.READ, bookmarks);

        transaction.rollback();
        transaction.close();
    }

    @Test
    public void shouldHaveAvailableBookmark() throws Exception {

        Transaction transaction = session.beginTransaction(Transaction.Type.READ_WRITE);

        transaction.commit();
        transaction.close();

        String lastBookmark = session.getLastBookmark();
        assertThat(lastBookmark).isEqualTo("last-bookmark");
    }
}
