/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session.capability;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.service.Components;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.IntegrationTestRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Luanne Misquitta
 */
public class LoadCapabilityTest {

    private static final Driver driver = Components.driver();

    @ClassRule
    public static final TestRule server = new IntegrationTestRule(driver);

	private Session session;
	private Long pleaseId;
	private Long beatlesId;

	@Before
	public void init() throws IOException {
		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.music");
		session = sessionFactory.openSession(driver);

		//Create some data
		Artist theBeatles = new Artist("The Beatles");
		Album please = new Album("Please Please Me");
		theBeatles.getAlbums().add(please);
		please.setArtist(theBeatles);
		session.save(theBeatles);

		pleaseId = please.getId();
		beatlesId = theBeatles.getId();
	}

	@After
	public void clearDatabase() {
		session.purgeDatabase();
	}

	/**
	 * @see DATAGRAPH-707
	 */
	@Test
	public void loadAllShouldRespectEntityType() {
		Collection<Artist> artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId));
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName());

		Collection<Album> albums = session.loadAll(Album.class, Collections.singletonList(beatlesId));
		assertEquals(0, albums.size());

		artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), 0);
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName());

		albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), 0);
		assertEquals(0, albums.size());


		artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"));
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName());

		albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"));
		assertEquals(0, albums.size());


		artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"),0);
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName());

		albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"),0);
		assertEquals(0, albums.size());


		artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new Pagination(0,5));
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName());

		albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new Pagination(0,5));
		assertEquals(0, albums.size());


		artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new Pagination(0,5),0);
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName());

		albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new Pagination(0,5),0);
		assertEquals(0, albums.size());


		artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0,5));
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName());

		albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0,5));
		assertEquals(0, albums.size());


		artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0,5),0);
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName());

		Artist bonJovi = new Artist("Bon Jovi");
		session.save(bonJovi);

		artists = session.loadAll(Artist.class, Arrays.asList(beatlesId,pleaseId, bonJovi.getId()), new SortOrder().add("name"), new Pagination(0,5),0);
		assertEquals(2, artists.size());

		artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0,5),0);
		assertEquals(1, artists.size());
		assertEquals("The Beatles", artists.iterator().next().getName()); //make sure Bon Jovi isn't returned as well

		albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0,5),0);
		assertEquals(0, albums.size());
	}


	/**
	 * @see DATAGRAPH-707
	 */
	@Test
	public void loadOneShouldRespectEntityType() {
		Artist artist = session.load(Artist.class, beatlesId);
		assertEquals("The Beatles", artist.getName());

		Album album = session.load(Album.class, beatlesId);
		assertNull(album);

		artist = session.load(Artist.class, beatlesId,0);
		assertEquals("The Beatles", artist.getName());

		album = session.load(Album.class, beatlesId,0);
		assertNull(album);

		artist = session.load(Artist.class, 10l); //ID does not exist
		assertNull(artist);


	}
}
