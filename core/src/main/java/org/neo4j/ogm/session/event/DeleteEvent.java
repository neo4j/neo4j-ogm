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
 * Calling session.delete(entity) on an object that is not persisted will fire no events
 *
 * Calling session.delete(entity) on an entity will fire two DeleteEvent s, before and after persisting; getTargetObjects() retrieves a list with only one object.
 *
 * Calling session.delete(<? extends Collection>) will fire a sequence of DeleteEvent s, before and after persisting; getTargetObjects() retrieves the list of all affected entities.
 *
 * @author Mihai Raulea
 */

public class DeleteEvent implements Event {

    public static String LIFECYCLE;
    public Object affectedObject;

    public static final String PRE  = "preSave";
    public static final String POST = "postSave";

    public Object getTargetObject() {
        return affectedObject;
    }
}
