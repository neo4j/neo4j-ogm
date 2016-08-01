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

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.FileSystemEntity;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;

import java.util.UUID;

/**
 * @author vince
 */
public class LifecycleTest extends EventTestBaseClass {

    @Test
    public void shouldSaveObjectUpdatedByPreSaveEventHandler() {

        session.dispose(eventListener); // not interested in the default one for this suite
        session.register(uuidEventListener);

        Folder f = new Folder();
        f.setName("folder");
        Assert.assertNull(f.getUuid());

        session.save(f);
        Assert.assertNotNull(f.getUuid());

        session.clear();

        Folder f2 = session.load(Folder.class, f.getId());

        Assert.assertNotNull(f2.getName());
        Assert.assertNotNull(f2.getUuid());

    }

    @Test
    public void shouldNotSaveObjectUpdatedByPreDeleteEventHandler() {

        session.dispose(eventListener); // not interested in the default one for this suite
        session.register(uuidEventListener);

        Folder f = new Folder();
        Document d = new Document();

        f.setName("folder");
        d.setFolder(f);
        f.getDocuments().add(d);

        Assert.assertNull(f.getUuid());

        session.save(f);
        Assert.assertNotNull(f.getUuid());

        //
        Assert.assertFalse( ((Neo4jSession) session).context().isDirty(f));

        session.delete(d);

        Assert.assertEquals("updated by pre-delete", f.getName());
        Assert.assertTrue(((Neo4jSession) session).context().isDirty(f));

        session.clear();

        Folder f2 = session.load(Folder.class, f.getId());

        Assert.assertTrue(f2.getDocuments().isEmpty());
        Assert.assertEquals("folder", f2.getName());

    }

    private EventListener uuidEventListener = new EventListener() {
        @Override
        public void onPreSave(Event event) {
            FileSystemEntity entity = (FileSystemEntity)event.getObject();
            if (entity.getId() == null) {
                entity.setUuid(UUID.randomUUID().toString());
            }
        }

        @Override
        public void onPostSave(Event event) {

        }

        @Override
        public void onPreDelete(Event event) {
            FileSystemEntity entity = (FileSystemEntity)event.getObject();
            if (entity instanceof Document) {
                Document d = (Document) entity;
                Folder f = d.getFolder();
                f.setName("updated by pre-delete");
            }
        }

        @Override
        public void onPostDelete(Event event) {

        }
    };


}
