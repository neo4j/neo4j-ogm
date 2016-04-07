/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.persistence.session.events;

import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author vince
 */
public class AssociatedObjectsTest extends EventTest {

    @Test
    public void shouldNotFireEventsOnAssociatedFolderThatHasNotChanged() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        // even though the document is updated,
        // its associated folder has not changed,
        // so no folder save events should fire
        a.setName("newA");
        session.save(a);

        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_SAVE));

        assertEquals(2, eventListenerTest.count());
    }

    @Test
    public void shouldFireEventsForAllDirtyObjectsThatAreReachableFromTheRoot() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        // the documents b and c are connected to a via a shared folder,
        // so events should fire for each of a,b and c
        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        session.save(a);

        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(b, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(b, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(c, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(c, Event.LIFECYCLE.POST_SAVE));

        assertEquals(6, eventListenerTest.count());

    }

    @Test
    public void shouldFireEventsForAssociatedObjectsWhenDeletingParentObjectWithInconsistentDomainModel() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        session.delete(a);  // a has a folder object reference

        // note that we're not actually changing the folder object. It still
        // has a reference to the deleted document a. This means the domain model
        // will be internally inconsistent. Nevertheless, because the relationship between
        // the folder and the document has been deleted in the graph, we must
        // fire events for the folder.

        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_DELETE));

        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));

        assertEquals(4, eventListenerTest.count());


    }

    @Test
    public void shouldFireEventsForAssociatedObjectsWhenDeletingParentObjectWithConsistentDomainModel() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        folder.getDocuments().remove(a);
        session.delete(a);  // a has a folder object reference

        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_DELETE));

        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));

        assertEquals(4, eventListenerTest.count());

    }

    @Test
    public void shouldFireEventsWhenAddNewObjectInCollectionAndSaveCollection() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        // add a new document to an existing folder and save the document
        Document z = new Document();
        z.setFolder(folder);
        folder.getDocuments().add(z);

        session.save(folder);

        assertTrue(eventListenerTest.captured(z, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(z, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));

        assertEquals(4, eventListenerTest.count());
    }

    @Test
    public void shouldFireEventsWhenAddNewObjectToCollectionAndSaveNewObject() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        // add a new document to an existing folder and save the document
        Document z = new Document();
        z.setFolder(folder);
        folder.getDocuments().add(z);

        session.save(z);

        assertTrue(eventListenerTest.captured(z, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(z, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));

        assertEquals(4, eventListenerTest.count());
    }

    @Test
    public void shouldFireEventsWhenAddExistingObjectToCollectionAndSaveExistingObject() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        d.setFolder(folder);
        folder.getDocuments().add(d);

        session.save(d);

        assertTrue(eventListenerTest.captured(d, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(d, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));

        assertEquals(4, eventListenerTest.count());

    }

    @Test
    public void shouldFireEventsWhenSetAssociatedObjectToNewAnonymousObject() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        a.setFolder(new Folder());
        folder.getDocuments().remove(a);

        session.save(a);

        // events for creation of new object
        assertTrue(eventListenerTest.captured(new Folder(), Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(new Folder(), Event.LIFECYCLE.POST_SAVE));

        // events for update of a's folder relationship
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_SAVE));

        // events for updates of folder's relationship to a
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));

        assertEquals(6, eventListenerTest.count());
    }

    @Test
    public void shouldFireEventsWhenAddExistingObjectToCollectionAndSaveCollection() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        assertNull(d.getFolder());

        d.setFolder(folder);
        folder.getDocuments().add(d);

        session.save(folder);

        assertTrue(eventListenerTest.captured(d, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(d, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));

        assertEquals(4, eventListenerTest.count());
    }

    @Test
    public void shouldFireEventsWhenItemDisassociatedFromContainerAndSaveContainer() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        folder.getDocuments().remove(a);
        a.setFolder(null);

        session.save(folder);

        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_SAVE));

        assertEquals(4, eventListenerTest.count());
    }

    @Test
    public void shouldFireEventsWhenItemDisassociatedFromContainerAndSaveItem() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        folder.getDocuments().remove(a);
        a.setFolder(null);

        session.save(a);

        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(folder, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_SAVE));

        assertEquals(4, eventListenerTest.count());
    }
}
