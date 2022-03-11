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
package org.neo4j.ogm.cypher.compiler;

import static java.util.Collections.*;

import java.util.*;
import java.util.function.Function;

import org.neo4j.ogm.compiler.SrcTargetKey;
import org.neo4j.ogm.context.Mappable;

/**
 * Maintains contextual information throughout the process of compiling Cypher statements to persist a graph of objects.
 *
 * @author Mark Angrish
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Andreas Berger
 * @author Michael J. Simons
 */
public class CypherContext implements CompileContext {

    /**
     * Stores the the builder and the horizon of visited objects. The key is either the native graph id of the entity
     * if the id is available (that is not null and greater 0L) or otherwise the entity itself.
     */
    private final Map<Object, NodeBuilderHorizonPair> visitedObjects = new IdentityHashMap<>();
    private final Set<Long> visitedRelationshipEntities = new HashSet<>();

    private final Map<Long, Object> createdObjectsWithId = new HashMap<>();
    private final Map<Long, Long> newNodeIds = new HashMap<>();

    private final Set<Mappable> registeredRelationships = new HashSet<>();
    private final Set<Mappable> deletedRelationships = new HashSet<>();

    private final Set<Object> registry = new HashSet<>();
    private final Map<SrcTargetKey, Set<Object>> transientRelsIndex = new HashMap<>();

    private final Compiler compiler;

    private final Function<Object, Long> nativeIdProvider;

    public CypherContext(Compiler compiler, Function<Object, Long> nativeIdProvider) {
        this.compiler = compiler;
        this.nativeIdProvider = nativeIdProvider;
    }

    private Object getIdentity(Object entity) {
        Long nativeId = this.nativeIdProvider.apply(entity);
        return (nativeId == null || nativeId < 0) ? entity : nativeId;
    }

    public boolean visited(Object entity, int horizon) {
        NodeBuilderHorizonPair pair = visitedObjects.get(getIdentity(entity));
        return pair != null && (horizon < 0 || pair.getHorizon() > horizon);
    }

    @Override
    public void visit(Object entity, NodeBuilder nodeBuilder, int horizon) {
        this.visitedObjects.put(getIdentity(entity), new NodeBuilderHorizonPair(nodeBuilder, horizon));
    }

    public void registerRelationship(Mappable mappedRelationship) {
        this.registeredRelationships.add(mappedRelationship);
    }

    public boolean removeRegisteredRelationship(Mappable mappedRelationship) {
        return this.registeredRelationships.remove(mappedRelationship);
    }

    @Override
    public NodeBuilder visitedNode(Object entity) {
        NodeBuilderHorizonPair pair = this.visitedObjects.get(getIdentity(entity));
        return pair != null ? pair.getNodeBuilder() : null;
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
        if (!registry.contains(object)) {
            registry.add(object);
        }
    }

    @Override
    public void registerTransientRelationship(SrcTargetKey key, Object object) {
        if (!registry.contains(object)) {
            registry.add(object);
            Set<Object> collection = transientRelsIndex.computeIfAbsent(key, k -> new HashSet<>());
            collection.add(object);
        }
    }

    public Collection<Object> registry() {
        return Collections.unmodifiableSet(registry);
    }

    /**
     * Invoked when the mapper wishes to mark a set of outgoing relationships to a specific type like (a)-[:T]-&gt;(*) as deleted, prior
     * to possibly re-establishing them individually as it traverses the entity graph.
     * There are two reasons why a set of relationships might not be be able to be marked deleted:
     * 1) the request to mark them as deleted has already been made
     * 2) the relationship is not persisted in the graph (i.e. its a new relationship)
     * Only case 1) is considered to be a failed request, because this context is only concerned about
     * pre-existing relationships in the graph. In order to distinguish between the two cases, we
     * also maintain a list of successfully deleted relationships, so that if we try to delete an already-deleted
     * set of relationships we can signal the error and undelete it.
     *
     * @param src              the identity of the node at the start of the relationship
     * @param relationshipType the type of the relationship
     * @param endNodeType      the class type of the entity at the end of the relationship
     * @return true if the relationship was deleted or doesn't exist in the graph, false otherwise
     */
    public boolean deregisterOutgoingRelationships(Long src, String relationshipType, Class endNodeType) {

        return deregisterRelationshipsImpl(src, relationshipType, endNodeType, Mappable::getStartNodeId,
            Mappable::getEndNodeType);
    }

    /**
     * Invoked when the mapper wishes to mark a set of incoming relationships to a specific type like (a)&lt;-[:T]-(*) as deleted, prior
     * to possibly re-establishing them individually as it traverses the entity graph.
     * There are two reasons why a set of relationships might not be be able to be marked deleted:
     * 1) the request to mark them as deleted has already been made
     * 2) the relationship is not persisted in the graph (i.e. its a new relationship)
     * Only case 1) is considered to be a failed request, because this context is only concerned about
     * pre-existing relationships in the graph. In order to distinguish between the two cases, we
     * also maintain a list of successfully deleted relationships, so that ff we try to delete an already-deleted
     * set of relationships we can signal the error and undelete it.
     *
     * @param tgt              the identity of the node at the pointy end of the relationship
     * @param relationshipType the type of the relationship
     * @param endNodeType      the class type of the entity at the other end of the relationship
     * @return true if the relationship was deleted or doesn't exist in the graph, false otherwise
     */
    public boolean deregisterIncomingRelationships(Long tgt, String relationshipType, Class endNodeType,
        boolean relationshipEntity) {

        Function<Mappable, Class> endNodeTypeExtractor = relationshipEntity ?
            Mappable::getEndNodeType :
            Mappable::getStartNodeType;

        return deregisterRelationshipsImpl(tgt, relationshipType, endNodeType, Mappable::getEndNodeId,
            endNodeTypeExtractor);
    }

    /**
     * Shared implementation for deregistering relationships for both
     * {@link #deregisterIncomingRelationships(Long, String, Class, boolean)} and
     * {@link #deregisterOutgoingRelationships(Long, String, Class)} methods. The extractors passed to this method here
     * are used to extract the relevant information of all candidates that might need to be deregistered. Candidates are
     * all registered relationships.
     *
     * @param nodeId                     the native id of the relationship to deregister
     * @param relationshipType           the type of the relationship to deregister
     * @param endNodeType                the node type of the entity at the other end of the relationship to deregister
     * @param candidateNodeIdExtractor   a function to extract the native id from a candidate relationship
     * @param candidateNodeTypeExtractor a function to extract the node type from a candidate relationship
     * @return true if the relationship was deleted or doesn't exist in the graph, false otherwise
     */
    private boolean deregisterRelationshipsImpl(Long nodeId, String relationshipType, Class endNodeType,
        Function<Mappable, Long> candidateNodeIdExtractor,
        Function<Mappable, Class> candidateNodeTypeExtractor) {

        List<Mappable> boundForDeletion = new ArrayList<>();
        Iterator<Mappable> candidatesForDeletion = this.registeredRelationships.iterator();

        boolean existsInGraph = false;
        while (candidatesForDeletion.hasNext()) {

            Mappable candidate = candidatesForDeletion.next();

            long candidateNodeId = candidateNodeIdExtractor.apply(candidate);
            String candidateRelationshipType = candidate.getRelationshipType();
            Class candidateNodeType = candidateNodeTypeExtractor.apply(candidate);

            if (candidateNodeId == nodeId &&
                candidateRelationshipType.equals(relationshipType) &&
                candidateNodeType.equals(endNodeType)) {

                existsInGraph = true;
                if (!isAlreadyDeleted(candidate)) {
                    boundForDeletion.add(candidate);
                    candidatesForDeletion.remove();
                }
            }
        }

        this.deletedRelationships.addAll(boundForDeletion);
        boolean aCandidateMarkedForDeletion = !boundForDeletion.isEmpty();

        return !existsInGraph || aCandidateMarkedForDeletion;
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
    public Collection<Object> getTransientRelationships(SrcTargetKey srcTargetKey) {
        Collection<Object> objects = transientRelsIndex.get(srcTargetKey);
        if (objects != null) {
            return objects;
        } else {
            return emptySet();
        }
    }

    /**
     * Checks whether a mapped relationship is already marked as deleted. This is the case when one of the relationships
     * marked as deleted contains a relationship starting and ending at the same nodes having the same relationship type.
     *
     * @param mappedRelationship The relationship that should be checked for being already marked as deleted or not
     * @return True if {@code mappedRelationship} was already marked as deleted
     */
    private boolean isAlreadyDeleted(Mappable mappedRelationship) {

        return deletedRelationships.contains(mappedRelationship);
    }

    private static class NodeBuilderHorizonPair {

        private final NodeBuilder nodeBuilder;
        private final int horizon;

        private NodeBuilderHorizonPair(NodeBuilder nodeBuilder, int horizon) {
            this.nodeBuilder = nodeBuilder;
            this.horizon = horizon;
        }

        public NodeBuilder getNodeBuilder() {
            return nodeBuilder;
        }

        public int getHorizon() {
            return horizon;
        }
    }
}
