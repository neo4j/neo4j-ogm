/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Frantisek Hartman
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("deprecation")
public class BookmarkTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Driver nativeDriver;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private org.neo4j.driver.Session nativeSession;
    private Session session;

    @Before
    public void setUp() {
        BoltDriver driver = new BoltDriver(nativeDriver);
        session = new Neo4jSession(new MetaData("org.neo4j.ogm.empty"), true, driver);
    }

    @Test
    public void shouldPassBookmarksToDriver() {
        Set<String> bookmarkStringRepresentation = new HashSet<>(Arrays.asList("bookmark1", "bookmark2"));

        Transaction transaction = session.beginTransaction(Transaction.Type.READ_ONLY, bookmarkStringRepresentation);
        ArgumentCaptor<SessionConfig> argumentCaptor = ArgumentCaptor.forClass(SessionConfig.class);

        verify(nativeDriver).session(argumentCaptor.capture());
        SessionConfig sessionConfig = argumentCaptor.getValue();
        assertThat(sessionConfig.defaultAccessMode()).isEqualTo(AccessMode.READ);
        assertThat(sessionConfig.bookmarks())
            .contains(
                Bookmark.from(Collections.singleton("bookmark1")),
                Bookmark.from(Collections.singleton("bookmark2"))
            );

        transaction.rollback();
        transaction.close();
    }

    @Test
    public void shouldPassMultiValueBookmarksToDriver() {
        Set<String> bookmarkStringRepresentation = new HashSet<>(Arrays.asList("bookmark1", "bookmark2", "bookmark3-part1BSbookmark3-part2"));

        Transaction transaction = session.beginTransaction(Transaction.Type.READ_ONLY, bookmarkStringRepresentation);
        ArgumentCaptor<SessionConfig> argumentCaptor = ArgumentCaptor.forClass(SessionConfig.class);

        verify(nativeDriver).session(argumentCaptor.capture());

        Set<String> multipleBookmarks = new HashSet<>();
        multipleBookmarks.addAll(Arrays.asList("bookmark3-part1", "bookmark3-part2"));
        SessionConfig sessionConfig = argumentCaptor.getValue();
        assertThat(sessionConfig.defaultAccessMode()).isEqualTo(AccessMode.READ);
        assertThat(sessionConfig.bookmarks())
            .contains(
                Bookmark.from(Collections.singleton("bookmark1")),
                Bookmark.from(Collections.singleton("bookmark2")),
                Bookmark.from(multipleBookmarks)
            );

        transaction.rollback();
        transaction.close();
    }

    @Test
    public void shouldHaveAvailableBookmark() {
        when(nativeDriver.session(any(SessionConfig.class))).thenReturn(nativeSession);
        when(nativeSession.beginTransaction().isOpen()).thenReturn(true);
        when(nativeSession.lastBookmark()).thenReturn(Bookmark.from(Collections.singleton("last-bookmark")));

        Transaction transaction = session.beginTransaction(Transaction.Type.READ_WRITE);

        transaction.commit();
        transaction.close();

        String lastBookmark = session.getLastBookmark();
        assertThat(lastBookmark).isEqualTo("last-bookmark");
    }

    /**
     * Make sure a bookmark containing multiple values is treated as one, not multiple bookmarks
     */
    @Test
    public void shouldDealWithMultiValueBookmarks() {
        Set<String> multipleBookmarks = new LinkedHashSet<>();
        multipleBookmarks.addAll(Arrays.asList("bookmark-part1", "bookmark-part2"));

        when(nativeDriver.session(any(SessionConfig.class))).thenReturn(nativeSession);
        when(nativeSession.beginTransaction().isOpen()).thenReturn(true);
        when(nativeSession.lastBookmark()).thenReturn(Bookmark.from(multipleBookmarks));

        Transaction transaction = session.beginTransaction(Transaction.Type.READ_WRITE);

        transaction.commit();
        transaction.close();

        String lastBookmark = session.getLastBookmark();
        assertThat(lastBookmark).isEqualTo("bookmark-part1BSbookmark-part2");
    }
}
