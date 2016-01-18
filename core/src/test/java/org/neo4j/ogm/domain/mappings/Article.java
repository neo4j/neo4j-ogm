/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.domain.mappings;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Nils Dr\u00F6ge
 */
public class Article extends Entity {
    @Relationship(type = "RELATED_TO", direction = "INCOMING")
    public Set<RichRelation> relations = new HashSet<>();

    @Relationship(type = "HAS")
    public List<Tag> tags;
}
