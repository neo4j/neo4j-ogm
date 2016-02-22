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

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class Author {
	@GraphId
	public Long id;

	@Relationship(type="AUTHORED_BY", direction = Relationship.INCOMING)
	public Set<Post> posts;

	@Relationship(type = "COMMENT_BY", direction = Relationship.INCOMING)
	public Set<Comment> comments = new HashSet<>();

	public Author() {
	}
}
