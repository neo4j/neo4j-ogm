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
package org.neo4j.ogm.typeconversion;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Michael J. Simons
 */
public class TypeConversionIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.music");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @After
    public void clear() {
        session.purgeDatabase();
    }

    @Test // GH-71
    public void convertibleReturnTypesShouldBeHandled() {
        final ZoneId utc = ZoneId.of("UTC");

        final Artist queen = new Artist("Queen");
        Album album = new Album("Queen");
        album.setArtist(queen);
        album.setRecordedAt(Date.from(LocalDate.of(1972, 11, 30).atStartOfDay(utc).toInstant()));
        album.setReleased(Date.from(LocalDate.of(1973, 7, 13).atStartOfDay(utc).toInstant()));

        album = new Album("Queen II");
        album.setArtist(queen);
        album.setRecordedAt(Date.from(LocalDate.of(1973, 8, 1).atStartOfDay(utc).toInstant()));
        final Date queen2ReleaseDate = Date.from(LocalDate.of(1974, 3, 8).atStartOfDay(utc).toInstant());
        album.setReleased(queen2ReleaseDate);

        session.save(album);
        session.clear();
        
        final Date latestReleases = session.queryForObject(Date.class, "MATCH (n:`l'album`) RETURN MAX(n.releasedAt)", new HashMap<>());
        Assertions.assertThat(latestReleases).isEqualTo(queen2ReleaseDate);
    }
}
