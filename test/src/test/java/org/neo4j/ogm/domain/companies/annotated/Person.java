/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

    @Relationship(type = "EMPLOYEE", direction = "OUTGOING")
    private Company employer;

    @Relationship(type = "OWNER", direction = "OUTGOING")
    private Set<Company> owns;

    @Relationship(type = "DEVICE", direction = Relationship.UNDIRECTED)
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
