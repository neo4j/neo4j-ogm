/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.domain.blog;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * This is a relationship entity only to test navigation in one direction. Do not make this a node entity.
 * @author Luanne Misquitta
 */
@RelationshipEntity(type = "COMMENT_BY")
public class Comment {
	@GraphId Long id;

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
