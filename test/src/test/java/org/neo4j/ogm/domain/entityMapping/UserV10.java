/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
 * One incoming and one outgoing relationship of the same type. Methods annotated, fields not annotated.
 *
 * @author Luanne Misquitta
 */
public class UserV10 extends Entity {

    @Relationship(type = "LIKES")
    private UserV10 likes;

    @Relationship(type = "LIKES", direction = "INCOMING")
    private UserV10 likedBy;

    public UserV10() {
    }

    public UserV10 getLikes() {
        return likes;
    }

    public void setLikes(UserV10 likes) {
        this.likes = likes;
    }

    public UserV10 getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(UserV10 likedBy) {
        this.likedBy = likedBy;
    }
}
