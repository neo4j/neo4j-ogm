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
package org.neo4j.ogm.domain.typed_relationships;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */

public class SomeEntity {

    private Long id;

    @Relationship(type = "IRRELEVANT", direction = Relationship.Direction.INCOMING)
    private TypedEntity<?> thing;

    @Relationship(type = "ALSO_IRRELEVANT", direction = Relationship.Direction.INCOMING)
    private List<TypedEntity<?>> moreThings;

    protected List<String> someOtherStuff;

    public Long getId() {
        return id;
    }

    public TypedEntity<?> getThing() {
        return thing;
    }

    public void setThing(TypedEntity<?> thing) {
        this.thing = thing;
    }

    public List<TypedEntity<?>> getMoreThings() {
        return moreThings;
    }

    public void setMoreThings(List<TypedEntity<?>> moreThings) {
        this.moreThings = moreThings;
    }

    public List<String> getSomeOtherStuff() {
        return someOtherStuff;
    }

    public void setSomeOtherStuff(List<String> someOtherStuff) {
        this.someOtherStuff = someOtherStuff;
    }
}
