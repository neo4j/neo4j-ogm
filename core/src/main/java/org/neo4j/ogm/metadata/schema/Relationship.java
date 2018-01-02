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

/**
 * Relationship in a {@link Schema}
 *
 * @author Frantisek Hartman
 */
public interface Relationship {

    /**
     * Type of this relationship
     */
    String type();

    /**
     * Direction of the relationship from given node
     *
     * @return direction
     */
    String direction(Node node);

    /**
     * Return the other node on this side of the relationship
     *
     * @param node node
     * @return the other node
     */
    Node other(Node node);
}
