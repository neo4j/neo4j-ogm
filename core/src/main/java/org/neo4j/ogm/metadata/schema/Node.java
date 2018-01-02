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

package org.neo4j.ogm.metadata.schema;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Node in a {@link Schema}
 *
 * @author Frantisek Hartman
 */
public interface Node {

    /**
     * Primary - the most specific - label of the node
     *
     * @return label
     */
    Optional<String> label();

    /**
     * Labels this node has, usually only 1
     *
     * @return labels
     */
    Collection<String> labels();

    /**
     * Relationships declared on this node
     * The key in the map is the a name of the relationship, not type. E.g. a field name in the class
     *
     * @return relationship
     */
    Map<String, Relationship> relationships();

}
