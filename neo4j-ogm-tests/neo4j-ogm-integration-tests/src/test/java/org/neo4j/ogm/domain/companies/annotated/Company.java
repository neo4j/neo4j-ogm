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

import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class Company {

    @Id @GeneratedValue
    Long id;
    private String name;

    @Relationship(type = "OWNER", direction = Relationship.Direction.INCOMING)
    private Set<Person> owners;

    @Relationship(type = "EMPLOYEE", direction = Relationship.Direction.INCOMING)
    private Set<Person> employees;

    public Company() {
    }

    public Company(String name) {
        this.name = name;
    }

    public Set<Person> getOwners() {
        return owners;
    }

    public void setOwners(Set<Person> owners) {
        this.owners = owners;
    }

    public Set<Person> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Person> employees) {
        this.employees = employees;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
