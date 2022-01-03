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
package org.neo4j.ogm.domain.properties;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
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
        ZIP_CODE {
            @Override
            void doSomething() {

            }
        };

        void doSomething() {

        }
    }
}
