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

package org.neo4j.ogm.compiler;

import java.util.Collection;
import java.util.Map;

import org.neo4j.ogm.model.Node;

/**
 * Builds a node to be persisted in the database.
 *
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public interface NodeBuilder {

    Long reference();

    NodeBuilder addProperty(String key, Object value);

    NodeBuilder addProperties(Map<String, ?> properties);

    NodeBuilder addLabels(Collection<String> labels);

    String[] addedLabels();

    NodeBuilder removeLabels(Collection<String> labels);

    Node node();

    NodeBuilder setPrimaryIndex(String primaryIndexField);
}
