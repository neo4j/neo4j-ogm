/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
