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
package org.neo4j.ogm.domain.gh806;

import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class Container {

    @Id @GeneratedValue
    Long id;

    String name;

    @Relationship(type = "RELATES_TO", direction = Relationship.Direction.INCOMING)
    Set<Element> element;

    @Relationship(type = "RELATES_TO_AS_WELL", direction = Relationship.Direction.INCOMING)
    Set<IElement> element2;

    /**
     * This is added to ensure we don't delete unrelated relationships.
     */
    @Relationship(type = "RELATES_TO_TOO", direction = Relationship.Direction.INCOMING)
    Set<ConcreteElement> elementsOfAnotherRelationship;

    public Container() {
    }

    public Container(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Element> getElement() {
        return element;
    }

    public void setElement(Set<Element> element) {
        this.element = element;
    }

    public Set<ConcreteElement> getElementsOfAnotherRelationship() {
        return elementsOfAnotherRelationship;
    }

    public void setElementsOfAnotherRelationship(
        Set<ConcreteElement> elementsOfAnotherRelationship) {
        this.elementsOfAnotherRelationship = elementsOfAnotherRelationship;
    }

    public Set<IElement> getElement2() {
        return element2;
    }

    public void setElement2(Set<IElement> element2) {
        this.element2 = element2;
    }
}
