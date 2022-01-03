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
package org.neo4j.ogm.domain.blog;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * This is a relationship entity only to test navigation in one direction. Do not make this a node entity.
 *
 * @author Luanne Misquitta
 */
@RelationshipEntity(type = "COMMENT_BY")
public class Comment {

    @Id @GeneratedValue Long id;

    @StartNode Post post;
    @EndNode Author author;

    String comment;

    public Comment() {
    }

    public Comment(Post post, Author author, String comment) {
        this.post = post;
        this.author = author;
        this.comment = comment;
    }
}
