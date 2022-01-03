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
package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies field that is to be represented as a relationship
 *
 * @author Vince Bickers
 * @author Frantisek Hartman
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Relationship {

    /**
     * Enumeration of the direction a relationship can take.
     */
    enum Direction {

        /**
         * Describes an outgoing relationship.
         */
        OUTGOING,

        /**
         * Describes an incoming relationship.
         */
        INCOMING,

        /**
         * Describes an undirected relationship.
         */
        UNDIRECTED

    }

    String TYPE = "type";
    String DIRECTION = "direction";

    // Alias for the enum constants. These allow for some compatibility between 3.2 and 4.0
    Direction INCOMING = Direction.INCOMING;
    Direction OUTGOING = Direction.OUTGOING;
    Direction UNDIRECTED = Direction.UNDIRECTED;

    @ValueFor(TYPE)
    String value() default "";

    /**
     * Type of the relationship, defaults to name of the field in SNAKE_CASE
     */
    String type() default "";

    /**
     * Direction of the relationship. Defaults to OUTGOING.
     * Possible values are {@link Direction#OUTGOING}, {@link Direction#INCOMING} and {@link Direction#UNDIRECTED}.
     */
    Direction direction() default Direction.OUTGOING;

}
