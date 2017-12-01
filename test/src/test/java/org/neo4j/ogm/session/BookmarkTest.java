/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session;

import static com.google.common.collect.Sets.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

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
        Set<String> bookmarks = newHashSet("bookmark1", "bookmark2");

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
