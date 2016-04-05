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

package org.neo4j.ogm.session.event;

/**
 * Calling session.save(entity) on an object that is not dirty(the in-memory representation of the object is the same as the object in the database) will fire no events
 *
 * Calling save(entity) on a dirty entity will fire two SaveEvent s, before and after updating; getTargetObject() retrieves a list with only one object.
 * If dirty connected entities are found, then each dirty entity will fire SaveEvent in the same manner
 *
 * Calling session.save(<? extends Collection>) will fire just two SaveEvent s, before and after persisting; getTargetObject() retrieves the list of all affected entities.
 * The connected dirty objects rule applies.
 *
 * Calling session.save(entity) on an entity that has new relationships will fire two SaveEvent s, before and after persisting; getTargetObjects() retrieves the list of TransientRelationship, one for each new relationship created
 * The connected dirty objects/entities rule applies.
 *
 * Calling session.save(relationshipEntity) altering a RelationshipEntity will fire a SaveEvent before and after persisting; each event will contain the relationship object, and a TransientRelationship. This is because internally, the OGM first deletes the relationship, and then recreates it.
 * The connected dirty objects/entities rule applies.
 *
 * @author Mihai Raulea
 */
public class SaveEvent implements Event {

    public static String LIFECYCLE;
    public Object affectedObject;

    public static final String PRE  = "preSave";
    public static final String POST = "postSave";

    public Object getTargetObject() {
        return affectedObject;
    }

}
