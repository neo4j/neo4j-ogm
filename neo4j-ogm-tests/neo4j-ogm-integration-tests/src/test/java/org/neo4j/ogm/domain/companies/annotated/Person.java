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
package org.neo4j.ogm.domain.companies.annotated;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class Person {

    @Id @GeneratedValue
    Long id;

    private String name;

    @Relationship(type = "EMPLOYEE", direction = Relationship.Direction.OUTGOING)
    private Company employer;

    @Relationship(type = "OWNER", direction = Relationship.Direction.OUTGOING)
    private Set<Company> owns;

    @Relationship(type = "DEVICE", direction = Relationship.Direction.UNDIRECTED)
    private Set<Device> devices;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getEmployer() {
        return employer;
    }

    public void setEmployer(Company employer) {
        this.employer = employer;
    }

    public Set<Company> getOwns() {
        return owns;
    }

    public void setOwns(Set<Company> owns) {
        this.owns = owns;
    }

    public void addDevice(Device device) {
        if (this.devices == null) {
            this.devices = new HashSet<>();
        }
        this.devices.add(device);
        device.setPerson(this);
    }

    public void removeDevice(Device device) {
        if (this.devices != null) {
            this.devices.remove(device);
            device.setPerson(null);
        }
    }

    public Set<Device> getDevices() {
        return devices;
    }

}
