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

import org.neo4j.ogm.context.Mappable;

import java.util.*;

/**
 * Maintains contextual information throughout the process of compiling Cypher statements to persist a graph of objects.
 *
 * @author Mark Angrish
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class CypherContext implements CompileContext {

    private final Map<Long, NodeBuilder> visitedObjects = new HashMap<>();
    private final Set<Long> visitedRelationshipEntities = new HashSet<>();

    private final Map<Long, Object> createdObjectsWithId = new HashMap<>();
    private final Collection<Mappable> registeredRelationships = new HashSet<>();
    private final Collection<Mappable> deletedRelationships = new HashSet<>();
    private final Map<Long, Long> newNodeIds = new HashMap<>();

    private final Collection<Object> log = new HashSet<>();

    private final Compiler compiler;

    public CypherContext(Compiler compiler) {
        this.compiler = compiler;
    }

    public boolean visited(Long obj) {
        return this.visitedObjects.containsKey(obj);
    }

    @Override
    public void visit(Long identity, NodeBuilder nodeBuilder) {
        this.visitedObjects.put(identity, nodeBuilder);
    }

    public void registerRelationship(Mappable mappedRelationship) {
        this.registeredRelationships.add(mappedRelationship);
    }

    public boolean removeRegisteredRelationship(Mappable mappedRelationship) {
        return this.registeredRelationships.remove(mappedRelationship);
    }

    @Override
    public NodeBuilder visitedNode(Long identity) {
        return this.visitedObjects.get(identity);
    }

    @Override
    public void registerNewObject(Long reference, Object entity) {
        createdObjectsWithId.put(reference, entity);
        register(entity);
    }

    @Override
    public Object getNewObject(Long id) {
        return createdObjectsWithId.get(id);
    }

    public void register(Object object) {
        if (!log.contains(object)) {
            log.add(object);
        }
    }

    public Collection<Object> registry() {
        return log;
    }

    /**
     * Invoked when the mapper wishes to mark a set of outgoing relationships to a specific type like (a)-[:T]-&gt;(*) as deleted, prior
     * to possibly re-establishing them individually as it traverses the entity graph.
     *
     * There are two reasons why a set of relationships might not be be able to be marked deleted:
     *
     * 1) the request to mark them as deleted has already been made
     * 2) the relationship is not persisted in the graph (i.e. its a new relationship)
     *
     * Only case 1) is considered to be a failed request, because this context is only concerned about
     * pre-existing relationships in the graph. In order to distinguish between the two cases, we
     * also maintain a list of successfully deleted relationships, so that if we try to delete an already-deleted
     * set of relationships we can signal the error and undelete it.
     *
     * @param src the identity of the node at the start of the relationship
     * @param relationshipType the type of the relationship
     * @param endNodeType the class type of the entity at the end of the relationship
     * @return true if the relationship was deleted or doesn't exist in the graph, false otherwise
     */
    public boolean deregisterOutgoingRelationships(Long src, String relationshipType, Class endNodeType) {
        Iterator<Mappable> iterator = registeredRelationships.iterator();
        boolean nothingToDelete = true;
        List<Mappable> cleared = new ArrayList<>();
        while (iterator.hasNext()) {
           Mappable mappedRelationship = iterator.next();
           if (mappedRelationship.getStartNodeId() == src && mappedRelationship.getRelationshipType().equals(relationshipType) && endNodeType.equals(mappedRelationship.getEndNodeType())) {
               cleared.add(mappedRelationship);
               iterator.remove();
               nothingToDelete = false;
           }
        }
        if (nothingToDelete) {
            return true; //relationships not in the graph, okay, we can return
        }

        //Check to see if the relationships were previously deleted, if so, restore them
        iterator = cleared.iterator();
        while(iterator.hasNext()) {
            Mappable mappedRelationship = iterator.next();
            if(isMappableAlreadyDeleted(mappedRelationship)) {
                registerRelationship(mappedRelationship);
                iterator.remove();
            }
            else {
                deletedRelationships.add(mappedRelationship);
            }
        }
        return cleared.size()>0;
    }

    /**
     * Invoked when the mapper wishes to mark a set of incoming relationships to a specific type like (a)&lt;-[:T]-(*) as deleted, prior
     * to possibly re-establishing them individually as it traverses the entity graph.
     *
     * There are two reasons why a set of relationships might not be be able to be marked deleted:
     *
     * 1) the request to mark them as deleted has already been made
     * 2) the relationship is not persisted in the graph (i.e. its a new relationship)
     *
     * Only case 1) is considered to be a failed request, because this context is only concerned about
     * pre-existing relationships in the graph. In order to distinguish between the two cases, we
     * also maintain a list of successfully deleted relationships, so that ff we try to delete an already-deleted
     * set of relationships we can signal the error and undelete it.
     *
     * @param tgt the identity of the node at the pointy end of the relationship
     * @param relationshipType the type of the relationship
     * @param endNodeType the class type of the entity at the other end of the relationship
     * @return true if the relationship was deleted or doesn't exist in the graph, false otherwise
     */
    public boolean deregisterIncomingRelationships(Long tgt, String relationshipType, Class endNodeType, boolean relationshipEntity) {
        Iterator<Mappable> iterator = registeredRelationships.iterator();
        List<Mappable> cleared = new ArrayList<>();
        boolean nothingToDelete = true;
        while (iterator.hasNext()) {
            Mappable mappedRelationship = iterator.next();
            if (mappedRelationship.getEndNodeId() == tgt && mappedRelationship.getRelationshipType().equals(relationshipType) && endNodeType.equals(relationshipEntity?mappedRelationship.getEndNodeType():mappedRelationship.getStartNodeType())) {
                cleared.add(mappedRelationship);
                iterator.remove();
                nothingToDelete=false;
            }
        }

        if (nothingToDelete) {
            return true; //relationships not in the graph, okay, we can return
        }

        //Check to see if the relationships were previously deleted, if so, restore them
        iterator = cleared.iterator();
        while(iterator.hasNext()) {
            Mappable mappedRelationship = iterator.next();
            if(isMappableAlreadyDeleted(mappedRelationship)) {
                registerRelationship(mappedRelationship);
                iterator.remove();
            }
            else {
                deletedRelationships.add(mappedRelationship);
            }
        }
        return cleared.size()>0;
    }


    public void visitRelationshipEntity(Long relationshipEntity) {
        visitedRelationshipEntities.add(relationshipEntity);

    }

    public boolean visitedRelationshipEntity(Long relationshipEntity) {
        return visitedRelationshipEntities.contains(relationshipEntity);
    }

    public Compiler getCompiler() {
        return compiler;
    }

    public Long getId(Long reference) {
        if (newNodeIds.containsKey(reference)) {
            return newNodeIds.get(reference);
        }
        return reference;
    }

    @Override
    public void registerNewId(Long reference, Long id) {
        newNodeIds.put(reference, id);
    }

    @Override
    public void deregister(NodeBuilder nodeBuilder) {
        compiler.unmap(nodeBuilder);
    }

    @Override
    public Collection<Mappable> getDeletedRelationships() {
        return deletedRelationships;
    }

    @Override
    public Object getVisitedObject(Long reference) {
        return visitedObjects.get(reference);
    }

    private boolean isMappableAlreadyDeleted(Mappable mappedRelationship) {
        for (Mappable deletedRelationship : deletedRelationships) {
            if (deletedRelationship.getEndNodeId() == mappedRelationship.getEndNodeId() && deletedRelationship.getStartNodeId() == mappedRelationship.getStartNodeId() && deletedRelationship.getRelationshipType().equals(mappedRelationship.getRelationshipType())) {
                return true;
            }
        }
        return false;
    }
}