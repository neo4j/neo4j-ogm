/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.cypher.compiler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.model.Node;

/**
 * Builds a node to be persisted in the database.
 *
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public interface NodeBuilder extends PropertyContainerBuilder<NodeBuilder> {

    Long reference();

    NodeBuilder addLabels(Collection<String> labels);

    Node node();

    NodeBuilder setPrimaryIndex(String primaryIndexField);

    NodeBuilder setPreviousDynamicLabels(Set<String> previousDynamicLabels);
}
