package org.neo4j.ogm.persistence.session.mappingContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.domain.music.*;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;

/**
 * @author Mihai Raulea
 * @see ISSUE-86
 */
public class SessionAndMappingContextTest extends MultiDriverTestClass {

    // i need a Neo4jSession because the session interface does not define the context() method
    private Neo4jSession session;
    private Album album1,album2,album3;
    private Artist artist1,artist2;
    private Recording recording;
    private ReleaseFormat releaseFormat;
    private Studio studio;

    @Before
    public void init() throws IOException {
        session = (Neo4jSession)new SessionFactory("org.neo4j.ogm.domain.music").openSession();

        artist1 = new Artist();
        artist1.setName("MainArtist");

        artist2 = new Artist();
        artist2.setName("GuestArtist");

        album1 = new Album();
        album1.setName("First");
        album1.setGuestArtist(artist2);

        album2 = new Album();
        album2.setName("Second");

        album3 = new Album();
        album3.setName("Third");

        artist1.addAlbum(album1);
        artist1.addAlbum(album2);
        artist1.addAlbum(album3);

        studio = new Studio();
        studio.setName("Studio");

        recording = new Recording();
        recording.setAlbum(album1);
        recording.setAlbum(album2);
        recording.setAlbum(album3);
        recording.setStudio(studio);
        recording.setYear(2001);

        session.save(artist1);
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    @Test
    public void disposeFromMappingContextOnDeleteTest() {
        MappingContext mappingContext = session.context();
        Assert.assertTrue(mappingContext.getNodeEntity(artist1.getId()).getClass() == Artist.class);
        session.delete(artist1);
        // check that the mapping context does not hold a refference to the artist1 entity anymore
        Object object = mappingContext.getNodeEntity(artist1.getId());
        Assert.assertTrue( object == null);

        // check that objects with refferences to the deleted object have been cleared
        Album retrievedAlbum1 = (Album)mappingContext.getNodeEntity(album1.getId());
        Assert.assertTrue( retrievedAlbum1.getArtist() == null );
    }




}
