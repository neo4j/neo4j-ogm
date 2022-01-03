/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.persistence.session.events;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.event.Event;

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

        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(2);
    }

    @Test
    public void shouldFireEventsForAllDirtyObjectsThatAreReachableFromTheRoot() {

        // the documents b and c are connected to a via a shared folder,
        // so events should fire for each of a,b and c
        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        session.save(a);

        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(b, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(b, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(c, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(c, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(6);
    }

    @Test
    public void shouldFireEventsForAssociatedObjectsWhenDeletingParentObjectWithInconsistentDomainModel() {

        session.delete(a);  // a has a folder object reference

        // note that we're not actually changing the folder object. It still
        // has a reference to the deleted document a. This means the domain model
        // will be internally inconsistent. Nevertheless, because the relationship between
        // the folder and the document has been deleted in the graph, we must
        // fire events for the folder.

        assertThat(eventListener.captured(a, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_DELETE)).isTrue();

        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(4);
    }

    @Test
    public void shouldFireEventsForAssociatedObjectsWhenDeletingParentObjectWithConsistentDomainModel() {

        folder.getDocuments().remove(a);
        session.delete(a);  // a has a folder object reference

        assertThat(eventListener.captured(a, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_DELETE)).isTrue();

        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(4);
    }

    @Test
    public void shouldFireEventsWhenAddNewObjectInCollectionAndSaveCollection() {

        // add a new document to an existing folder and save the document
        Document z = new Document();
        z.setFolder(folder);
        folder.getDocuments().add(z);

        session.save(folder);

        assertThat(eventListener.captured(z, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(z, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(4);
    }

    @Test
    public void shouldFireEventsWhenAddNewObjectToCollectionAndSaveNewObject() {

        // add a new document to an existing folder and save the document
        Document z = new Document();
        z.setFolder(folder);
        folder.getDocuments().add(z);

        session.save(z);

        assertThat(eventListener.captured(z, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(z, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(4);
    }

    @Test
    public void shouldFireEventsWhenAddExistingObjectToCollectionAndSaveExistingObject() {

        d.setFolder(folder);
        folder.getDocuments().add(d);

        session.save(d);

        assertThat(eventListener.captured(d, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(d, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(4);
    }

    @Test
    public void shouldFireEventsWhenSetAssociatedObjectToNewAnonymousObject() {

        a.setFolder(new Folder());
        folder.getDocuments().remove(a);

        session.save(a);

        // events for creation of new object
        assertThat(eventListener.captured(new Folder(), Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(new Folder(), Event.TYPE.POST_SAVE)).isTrue();

        // events for update of a's folder relationship
        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();

        // events for updates of folder's relationship to a
        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(6);
    }

    @Test
    public void shouldFireEventsWhenAddExistingObjectToCollectionAndSaveCollection() {

        assertThat(d.getFolder()).isNull();

        d.setFolder(folder);
        folder.getDocuments().add(d);

        session.save(folder);

        assertThat(eventListener.captured(d, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(d, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(4);
    }

    @Test
    public void shouldFireEventsWhenItemDisassociatedFromContainerAndSaveContainer() {

        folder.getDocuments().remove(a);
        a.setFolder(null);

        session.save(folder);

        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(4);
    }

    @Test
    public void shouldFireEventsWhenItemDisassociatedFromContainerAndSaveItem() {

        folder.getDocuments().remove(a);
        a.setFolder(null);

        session.save(a);

        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(4);
    }
}
