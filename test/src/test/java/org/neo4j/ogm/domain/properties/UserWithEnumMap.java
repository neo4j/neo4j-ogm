/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.domain.properties;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frantisek Hartman
 */
@NodeEntity(label = "User")
public class UserWithEnumMap {

    private Long id;

    String name;

    @Properties
    private Map<UserProperties, Object> myProperties;

    public UserWithEnumMap() {
    }

    public UserWithEnumMap(String name) {
        this.name = name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<UserProperties, Object> getMyProperties() {
        return myProperties;
    }

    public void setMyProperties(Map<UserProperties, Object> myProperties) {
        this.myProperties = myProperties;
    }

    public void putMyProperty(UserProperties key, Object value) {
        if (myProperties == null) {
            myProperties = new HashMap<>();
        }
        myProperties.put(key, value);
    }

    public enum UserProperties {

        CITY,
        ZIP_CODE,


    }
}
