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

package org.neo4j.ogm.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to help group elements of a common type into a single collection (by relationship type and direction) to be set on an owning object.
 * The ability to set a collection of instances on an owning entity based on the type of instance is insufficient as described in DATAGRAPH-637, DATAGRAPH-636 and Issue 161.
 * The relationship type and direction as well as the type of entity to be mapped are required to be able to correctly determine which instances are to be set for which property of the node entity.
 * @author Adam George
 * @author Luanne Misquitta
 */
class EntityCollector {

    private final Map<Long, Map<DirectedRelationship, Map<Class, Set<Object>>>> relationshipCollectibles = new HashMap<>(); //TODO bad, we'll never remember what this does in a months time!! refactor this class!

    /**
     * Adds the given collectible element into a collection based on relationship type and direction ready to be set on the given owning type.
     *
     * @param owningEntityId        The id of the instance on which the collection is to be set
     * @param collectibleElement    The element to add to the collection that will eventually be set on the owning type
     * @param relationshipType      The relationship type that this collection corresponds to
     * @param relationshipDirection The relationship direction
     */
    public void recordTypeRelationship(Long owningEntityId, Object collectibleElement, String relationshipType, String relationshipDirection) {
        this.relationshipCollectibles.computeIfAbsent(owningEntityId, k -> new HashMap<>());
        DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType,relationshipDirection);
        this.relationshipCollectibles.get(owningEntityId).computeIfAbsent(directedRelationship, k -> new HashMap<>());
        this.relationshipCollectibles.get(owningEntityId).get(directedRelationship).computeIfAbsent(collectibleElement.getClass(), k -> new HashSet<>());
        this.relationshipCollectibles.get(owningEntityId).get(directedRelationship).get(collectibleElement.getClass()).add(collectibleElement);
    }

    /**
     * @return All the owning entity ids that have been added to this {@link EntityCollector}
     */
    public Iterable<Long> getOwningTypes() {
        return this.relationshipCollectibles.keySet();
    }

    /**
     * Retrieves all relationship types for which collectibles can be set on an owning object
     *
     * @param owningObjectId the owning object id
     * @return all relationship types owned by the owning object
     */
    public Iterable<String> getOwningRelationshipTypes(Long owningObjectId) {
        Set<String> relTypes = new HashSet<>();
        for(DirectedRelationship rel : this.relationshipCollectibles.get(owningObjectId).keySet()) {
            relTypes.add(rel.type());
        }
        return relTypes;
    }

    public Iterable<String> getRelationshipDirectionsForOwningTypeAndRelationshipType(Long owningObjectId, String relationshipType) {
        Set<String> relDirections = new HashSet<>();
        for(DirectedRelationship rel : this.relationshipCollectibles.get(owningObjectId).keySet()) {
            if(rel.type().equals(relationshipType)) {
                relDirections.add(rel.direction());
            }
        }
        return relDirections;
    }

    public Iterable<Class> getEntityClassesForOwningTypeAndRelationshipTypeAndRelationshipDirection(Long owningObjectId, String relationshipType, String relationshipDirection) {
        Set<Class> classes = new HashSet<>();
        for(DirectedRelationship rel : this.relationshipCollectibles.get(owningObjectId).keySet()) {
            if(rel.type().equals(relationshipType) && rel.direction().equals(relationshipDirection)) {
                classes.addAll(this.relationshipCollectibles.get(owningObjectId).get(rel).keySet());
            }
        }
        return classes;
    }
    /**
     * A set of collectibles based on relationship type for an owning object
     *
     * @param owningObjectId        the owning object id
     * @param relationshipType      the relationship type
     * @param relationshipDirection the relationship direction
     * @param entityClass           the entity class
     * @return set of instances to be set for the relationship type on the owning object
     */
    public Set<Object> getCollectiblesForOwnerAndRelationship(Long owningObjectId, String relationshipType, String relationshipDirection, Class entityClass) {
        DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType,relationshipDirection);
        return this.relationshipCollectibles.get(owningObjectId).get(directedRelationship).get(entityClass);
    }
}
