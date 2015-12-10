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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.IntegrationTestRule;

/**
 * @author Luanne Misquitta
 */
public class SaveCapabilityTest {
	private static final Driver driver = Components.driver();

	@ClassRule
	public static final TestRule server = new IntegrationTestRule(driver);

	private Session session;
	private Artist aerosmith;
	private Artist bonJovi;
	private Artist defLeppard;

	@Before
	public void init() throws IOException {
		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.music");
		session = sessionFactory.openSession(driver);

		aerosmith = new Artist("Aerosmith");
		bonJovi = new Artist("Bon Jovi");
		defLeppard = new Artist("Def Leppard");
	}

	@After
	public void clearDatabase() {
		session.purgeDatabase();
	}

	/**
	 * @see Issue #84
	 */
	@Test
	public void saveCollectionShouldSaveLists() {
		List<Artist> artists = Arrays.asList(aerosmith, bonJovi, defLeppard);
		session.save(artists);
		session.clear();
		assertEquals(3, session.countEntitiesOfType(Artist.class));
	}

	/**
	 * @see Issue #84
	 */
	@Test
	public void saveCollectionShouldSaveSets() {
		Set<Artist> artists = new HashSet<>();
		artists.add(aerosmith);
		artists.add(bonJovi);
		artists.add(defLeppard);
		session.save(artists);
		session.clear();
		assertEquals(3, session.countEntitiesOfType(Artist.class));
	}


	/**
	 * @see Issue #84
	 */
	@Test
	public void saveCollectionShouldSaveArrays() {
		Artist[] artists = new Artist[] {aerosmith, bonJovi, defLeppard};
		session.save(artists);
		session.clear();
		assertEquals(3, session.countEntitiesOfType(Artist.class));
	}

	@Test
	public void shouldSaveNewNodesAndNewRelationships() {
		Artist leann = new Artist("Leann Rimes");
		Album lost = new Album("Lost Highway");
		lost.setArtist(bonJovi);
		lost.setGuestArtist(leann);
		session.save(lost);
		session.clear();

		Artist loadedLeann = session.load(Artist.class, leann.getId());
		assertNotNull(loadedLeann);
		assertEquals("Leann Rimes", loadedLeann.getName());
		assertEquals(lost.getName(), loadedLeann.getGuestAlbums().iterator().next().getName());

		Artist loadedBonJovi = session.load(Artist.class, bonJovi.getId());
		assertNotNull(loadedBonJovi);
		assertEquals("Bon Jovi", loadedBonJovi.getName());
		assertEquals(lost.getName(), loadedBonJovi.getAlbums().iterator().next().getName());

		Album loadedLost = session.load(Album.class, lost.getId());
		assertNotNull(loadedLost);
		assertEquals("Lost Highway", loadedLost.getName());
		assertEquals(loadedLeann, loadedLost.getGuestArtist());
		assertEquals(loadedBonJovi.getName(), loadedLost.getArtist().getName());
	}

}
