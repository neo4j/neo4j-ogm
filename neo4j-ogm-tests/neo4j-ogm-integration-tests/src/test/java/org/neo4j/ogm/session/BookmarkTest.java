/*
 * Copyright (c) 2002-2025 "Neo4j,"
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
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Frantisek Hartman
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("deprecation")
public class BookmarkTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Driver nativeDriver;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private org.neo4j.driver.Session nativeSession;
    private Session session;

    @BeforeEach
    public void setUp() {
        BoltDriver driver = new BoltDriver(nativeDriver);
        session = new Neo4jSession(new MetaData("org.neo4j.ogm.empty"), true, driver);
    }

    @Test
    void shouldPassBookmarksToDriver() {
        Set<String> bookmarkStringRepresentation = Set.of("bookmark1", "bookmark2");

        Transaction transaction = session.beginTransaction(Transaction.Type.READ_ONLY, bookmarkStringRepresentation);
        ArgumentCaptor<SessionConfig> argumentCaptor = ArgumentCaptor.forClass(SessionConfig.class);

        verify(nativeDriver).session(argumentCaptor.capture());
        SessionConfig sessionConfig = argumentCaptor.getValue();
        assertThat(sessionConfig.defaultAccessMode()).isEqualTo(AccessMode.READ);
        assertThat(sessionConfig.bookmarks())
            .contains(
                Bookmark.from("bookmark1"),
                Bookmark.from("bookmark2")
            );

        transaction.rollback();
        transaction.close();
    }

    @Test
    void shouldHaveAvailableBookmark() {
        when(nativeDriver.session(any(SessionConfig.class))).thenReturn(nativeSession);
        when(nativeSession.beginTransaction(any(TransactionConfig.class)).isOpen()).thenReturn(true);
        when(nativeSession.lastBookmarks()).thenReturn(Set.of(Bookmark.from("last-bookmark")));

        Transaction transaction = session.beginTransaction(Transaction.Type.READ_WRITE);

        transaction.commit();
        transaction.close();

        String lastBookmark = session.getLastBookmark();
        assertThat(lastBookmark).isEqualTo("last-bookmark");
    }

}
