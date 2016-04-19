package org.neo4j.ogm.persistence.session.mappingContext;

import org.junit.*;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotations.DefaultEntityAccessStrategy;
import org.neo4j.ogm.annotations.PropertyReader;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.domain.music.*;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Mihai Raulea
 * @see ISSUE-86
 */
@Ignore
public class SessionAndMappingContextTest extends MultiDriverTestClass {

    // i need a Neo4jSession because the session interface does not define the context() method
    private Neo4jSession session;

    private Album album1,album2,album3;
    private Artist artist1,artist2;
    private Recording recording;
    private ReleaseFormat releaseFormat;
    private Studio studio;

    private Actor actor1;
    private Actor actor2;
    private Knows knows;



    @Before
    public void init() throws IOException {
        session = (Neo4jSession)new SessionFactory("org.neo4j.ogm.domain.music","org.neo4j.ogm.domain.cineasts.annotated").openSession();

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

        actor1 = new Actor("Actor1");
        actor2 = new Actor("Actor2");
        knows = new Knows();
        knows.setFirstActor(actor1);
        knows.setSecondActor(actor2);
        actor1.knows.add(knows);
        session.save(actor1);
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    @Test
    public void disposeFromMappingContextOnDeleteWithTransientRelationshipTest() {
        MappingContext mappingContext = session.context();
        Assert.assertTrue(mappingContext.getNodeEntity(artist1.getId()).getClass() == Artist.class);
        session.delete(artist1);

        // check that the mapping context does not hold a refference to the deleted entity anymore
        Object object = mappingContext.getNodeEntity(artist1.getId());
        Assert.assertTrue( object == null);

        // check that objects with refferences to the deleted object have been cleared
        // check for TransientRelationship, where the object connected to the deleted object holds ref in a Set
        Album retrievedAlbum1 = (Album)mappingContext.getNodeEntity(album1.getId());
        Assert.assertTrue( retrievedAlbum1.getArtist() == null );

        Album retrievedAlbum2 = (Album)mappingContext.getNodeEntity(album2.getId());
        Assert.assertTrue( retrievedAlbum2.getArtist() == null );

        Album retrievedAlbum3 = (Album)mappingContext.getNodeEntity(album3.getId());
        Assert.assertTrue( retrievedAlbum3.getArtist() == null );
    }

    /*
     * @see ISSUE-86
     */
    @Test
    public void disposeFromMappingContextOnDeleteWithRelationshipEntityTest() {
        Assert.assertTrue(session.context().getNodeEntity(actor1.getId()).getClass() == Actor.class);
        Object objectRel = session.context().getRelationshipEntity(knows.id);
        Assert.assertTrue(objectRel.getClass() == Knows.class);

        session.delete(actor1);

        Result result = session.query("MATCH N RETURN N", Collections.EMPTY_MAP);
        // check that the mapping context does not hold a refference to the deleted entity anymore
        Object object = session.context().getNodeEntity(actor1.getId());
        Assert.assertTrue( object == null);
        // check for a defined RelationshipEntity; the relationship should also be removed from the mappingContext
        objectRel = session.context().getRelationshipEntity(knows.id);
        Assert.assertTrue(objectRel == null);
        Assert.assertTrue(session.context().getNodeEntity(actor1.getId()) == null);
        // does it exist in the session?
        Knows inSessionKnows = session.load(Knows.class, knows.id);
        Assert.assertTrue(inSessionKnows == null);
    }

    @Test
    public void testEntityRelationshipProperlyRemoved() {
        session.delete(knows);
        Knows testKnows = session.load(Knows.class, knows.id);
        Assert.assertTrue(testKnows == null);
    }

    @Test
    public void testDetachNode() {
        Assert.assertTrue(session.detach(actor1.getId()));
        Assert.assertFalse(session.detach(actor1.getId()));
    }

    @Test
    public void testDetachRelationshipEntity() {
        Assert.assertTrue(session.detach(knows.id));
        Assert.assertFalse(session.detach(knows.id));
    }


}
