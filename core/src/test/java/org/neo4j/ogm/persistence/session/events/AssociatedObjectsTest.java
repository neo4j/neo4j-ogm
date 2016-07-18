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
public class AssociatedObjectsTest extends EventTestBaseClass {

    @Test
    public void shouldNotFireEventsOnAssociatedFolderThatHasNotChanged() {

        // even though the document is updated,
        // its associated folder has not changed,
        // so no folder save events should fire
        a.setName("newA");
        session.save(a);

        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));

        assertEquals(2, eventListener.count());
    }

    @Test
    public void shouldFireEventsForAllDirtyObjectsThatAreReachableFromTheRoot() {

        // the documents b and c are connected to a via a shared folder,
        // so events should fire for each of a,b and c
        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        session.save(a);

        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(b, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(b, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(c, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(c, Event.TYPE.POST_SAVE));

        assertEquals(6, eventListener.count());

    }

    @Test
    public void shouldFireEventsForAssociatedObjectsWhenDeletingParentObjectWithInconsistentDomainModel() {

        session.delete(a);  // a has a folder object reference

        // note that we're not actually changing the folder object. It still
        // has a reference to the deleted document a. This means the domain model
        // will be internally inconsistent. Nevertheless, because the relationship between
        // the folder and the document has been deleted in the graph, we must
        // fire events for the folder.

        assertTrue(eventListener.captured(a, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_DELETE));

        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));

        assertEquals(4, eventListener.count());


    }

    @Test
    public void shouldFireEventsForAssociatedObjectsWhenDeletingParentObjectWithConsistentDomainModel() {

        folder.getDocuments().remove(a);
        session.delete(a);  // a has a folder object reference

        assertTrue(eventListener.captured(a, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_DELETE));

        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));

        assertEquals(4, eventListener.count());

    }

    @Test
    public void shouldFireEventsWhenAddNewObjectInCollectionAndSaveCollection() {

        // add a new document to an existing folder and save the document
        Document z = new Document();
        z.setFolder(folder);
        folder.getDocuments().add(z);

        session.save(folder);

        assertTrue(eventListener.captured(z, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(z, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));

        assertEquals(4, eventListener.count());
    }

    @Test
    public void shouldFireEventsWhenAddNewObjectToCollectionAndSaveNewObject() {

        // add a new document to an existing folder and save the document
        Document z = new Document();
        z.setFolder(folder);
        folder.getDocuments().add(z);

        session.save(z);

        assertTrue(eventListener.captured(z, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(z, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));

        assertEquals(4, eventListener.count());
    }

    @Test
    public void shouldFireEventsWhenAddExistingObjectToCollectionAndSaveExistingObject() {

        d.setFolder(folder);
        folder.getDocuments().add(d);

        session.save(d);

        assertTrue(eventListener.captured(d, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(d, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));

        assertEquals(4, eventListener.count());

    }

    @Test
    public void shouldFireEventsWhenSetAssociatedObjectToNewAnonymousObject() {

        a.setFolder(new Folder());
        folder.getDocuments().remove(a);

        session.save(a);

        // events for creation of new object
        assertTrue(eventListener.captured(new Folder(), Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(new Folder(), Event.TYPE.POST_SAVE));

        // events for update of a's folder relationship
        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));

        // events for updates of folder's relationship to a
        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));

        assertEquals(6, eventListener.count());
    }

    @Test
    public void shouldFireEventsWhenAddExistingObjectToCollectionAndSaveCollection() {

        assertNull(d.getFolder());

        d.setFolder(folder);
        folder.getDocuments().add(d);

        session.save(folder);

        assertTrue(eventListener.captured(d, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(d, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));

        assertEquals(4, eventListener.count());
    }

    @Test
    public void shouldFireEventsWhenItemDisassociatedFromContainerAndSaveContainer() {

        folder.getDocuments().remove(a);
        a.setFolder(null);

        session.save(folder);

        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));

        assertEquals(4, eventListener.count());
    }

    @Test
    public void shouldFireEventsWhenItemDisassociatedFromContainerAndSaveItem() {

        folder.getDocuments().remove(a);
        a.setFolder(null);

        session.save(a);

        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));

        assertEquals(4, eventListener.count());
    }
}
