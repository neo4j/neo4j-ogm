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
package org.neo4j.ogm.domain.entityMapping;

import org.neo4j.ogm.annotation.Relationship;

/**
 * One outgoing and one incoming relationship of the same type. Incoming field and methods annotated. Outgoing field annotated (implied outgoing), methods not annotated.
 *
 * @author Luanne Misquitta
 */
public class UserV9 extends Entity {

    @Relationship(type = "LIKES")
    private UserV9 likes;

    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private UserV9 likedBy;

    public UserV9() {
    }

    public UserV9 getLikes() {
        return likes;
    }

    public void setLikes(UserV9 likes) {
        this.likes = likes;
    }

    public UserV9 getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(UserV9 likedBy) {
        this.likedBy = likedBy;
    }
}
