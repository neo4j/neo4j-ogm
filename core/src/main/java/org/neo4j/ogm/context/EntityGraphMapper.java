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
package org.neo4j.ogm.context;

import static org.neo4j.ogm.session.request.strategy.impl.NodeQueryStatements.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.compiler.SrcTargetKey;
import org.neo4j.ogm.cypher.compiler.CompileContext;
import org.neo4j.ogm.cypher.compiler.Compiler;
import org.neo4j.ogm.cypher.compiler.MultiStatementCypherCompiler;
import org.neo4j.ogm.cypher.compiler.NodeBuilder;
import org.neo4j.ogm.cypher.compiler.PropertyContainerBuilder;
import org.neo4j.ogm.cypher.compiler.RelationshipBuilder;
import org.neo4j.ogm.exception.core.InvalidRelationshipTargetException;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.support.CollectionUtils;
import org.neo4j.ogm.utils.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link EntityMapper} that is driven by an instance of {@link MetaData}.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class EntityGraphMapper implements EntityMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityGraphMapper.class);

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final Compiler compiler;
    /**
     * This tracks the current depth of mapping. While the session uses "depth" as external language, the
     * compiler context uses horizon. This here is the current depth of the mapping: 0 being the entrance at {@link #map(Object, int)},
     * incremented by 1 one each time entering {@link #mapEntityReferences(Object, NodeBuilder, int)} and decremented
     * accordingly. This is ugly and is yet one more state, but is needed to decide whether a root object is allowed to
     * overwrite relationships again.
     */
    private final AtomicInteger currentDepth = new AtomicInteger(0);

    /**
     * Default supplier for write protection: Always write all the stuff.
     */
    private Optional<BiFunction<WriteProtectionTarget, Class<?>, Predicate<Object>>> optionalWriteProtectionSupplier = Optional
        .empty();

    /**
     * Constructs a new {@link EntityGraphMapper} that uses the given {@link MetaData}.
     *
     * @param metaData       The {@link MetaData} containing the mapping information
     * @param mappingContext The {@link MappingContext} for the current session
     */
    public EntityGraphMapper(MetaData metaData, MappingContext mappingContext) {
        this.metaData = metaData;
        this.mappingContext = mappingContext;
        this.compiler = new MultiStatementCypherCompiler(mappingContext::nativeId);
    }

    public void addWriteProtection(
        BiFunction<WriteProtectionTarget, Class<?>, Predicate<Object>> writeProtectionSupplier) {

        this.optionalWriteProtectionSupplier = Optional.ofNullable(writeProtectionSupplier);
    }

    @Override
    public CompileContext map(Object entity) {
        return map(entity, -1);
    }

    @Override
    public CompileContext map(Object entity, int horizon) {

        this.currentDepth.set(0);

        if (entity == null) {
            throw new NullPointerException("Cannot map null object");
        }

        // add all the relationships we know about. This includes the relationships that
        // won't be modified by the mapping request.
        for (MappedRelationship mappedRelationship : mappingContext.getRelationships()) {
            LOGGER.debug("context-init: ({})-[:{}]->({})", mappedRelationship.getStartNodeId(),
                mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
            compiler.context().registerRelationship(mappedRelationship);
        }

        LOGGER.debug("context initialised with {} relationships", mappingContext.getRelationships().size());

        // if the object is a RelationshipEntity, persist it by persisting both the start node and the end node
        // and then ensure the relationship between the two is created or updated as necessary
        if (isRelationshipEntity(entity)) {

            ClassInfo reInfo = metaData.classInfo(entity);

            Object startNode = reInfo.getStartNodeReader().read(entity);
            if (startNode == null) {
                throw new RuntimeException("@StartNode of relationship entity may not be null");
            }

            Object endNode = reInfo.getEndNodeReader().read(entity);
            if (endNode == null) {
                throw new RuntimeException("@EndNode of relationship entity may not be null");
            }

            // map both sides as far as the specified horizon
            NodeBuilder startNodeBuilder = mapEntity(startNode, horizon);
            NodeBuilder endNodeBuilder = mapEntity(endNode, horizon);

            // create or update the relationship if its not already been visited in the current compile context
            if (!compiler.context().visitedRelationshipEntity(mappingContext.nativeId(entity))) {

                AnnotationInfo annotationInfo = reInfo.annotationsInfo().get(RelationshipEntity.class);
                String relationshipType = annotationInfo.get(RelationshipEntity.TYPE, null);
                DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType,
                    Direction.OUTGOING);

                RelationshipBuilder relationshipBuilder = getRelationshipBuilder(compiler, entity, directedRelationship, mappingContext.isDirty(entity));

                // 2. create or update the actual relationship (edge) in the graph
                updateRelationshipEntity(compiler.context(), entity, relationshipBuilder, reInfo);

                Long srcIdentity = mappingContext.nativeId(startNode);
                Long tgtIdentity = mappingContext.nativeId(endNode);

                RelationshipNodes relNodes = new RelationshipNodes(srcIdentity, tgtIdentity, startNode.getClass(),
                    endNode.getClass());

                // 2. update the fact of the relationship in the compile context
                updateRelationship(compiler.context(), startNodeBuilder, endNodeBuilder, relationshipBuilder, relNodes);
            }
        } else { // not an RE, simply map the entity
            mapEntity(entity, horizon);
        }

        deleteObsoleteRelationships();

        return compiler.context();
    }

    @Override
    public CompileContext compileContext() {
        return compiler.context();
    }

    /**
     * Detects object references (including from lists) that have been deleted in the domain.
     * These must be persisted as explicit requests to delete the corresponding relationship in the graph
     */
    private void deleteObsoleteRelationships() {
        CompileContext context = compiler.context();

        Map<Long, Object> snapshotOfKnownRelationshipEntities
            = mappingContext.getSnapshotOfRelationshipEntityRegister();
        Iterator<MappedRelationship> mappedRelationshipIterator = mappingContext.getRelationships().iterator();
        while (mappedRelationshipIterator.hasNext()) {
            MappedRelationship mappedRelationship = mappedRelationshipIterator.next();

            // if we cannot remove this relationship from the compile context, it
            // means the user has deleted the relationship
            if (!context.removeRegisteredRelationship(mappedRelationship)) {

                LOGGER.debug("context-del: {}", mappedRelationship);

                // tell the compiler to prepare a statement that will delete the relationship from the graph
                RelationshipBuilder builder = compiler.unrelate(
                    mappedRelationship.getStartNodeId(),
                    mappedRelationship.getRelationshipType(),
                    mappedRelationship.getEndNodeId(),
                    mappedRelationship.getRelationshipId());

                Object entity = snapshotOfKnownRelationshipEntities.get(mappedRelationship.getRelationshipId());
                if (entity != null) {
                    ClassInfo classInfo = metaData.classInfo(entity);
                    if (classInfo.hasVersionField()) {
                        FieldInfo field = classInfo.getVersionField();
                        builder.setVersionProperty(field.propertyName(), (Long) field.read(entity));
                    }
                }

                // remove all nodes that are referenced by this relationship in the mapping context
                // this will ensure that stale versions of these objects don't exist
                clearRelatedObjects(mappedRelationship.getStartNodeId());
                clearRelatedObjects(mappedRelationship.getEndNodeId());

                // finally remove the relationship from the mapping context
                //mappingContext.removeRelationship(mappedRelationship);
                mappedRelationshipIterator.remove();
            }
        }
    }

    private void clearRelatedObjects(Long node) {

        for (MappedRelationship mappedRelationship : mappingContext.getRelationships()) {
            if (mappedRelationship.getStartNodeId() == node || mappedRelationship.getEndNodeId() == node) {

                Object dirty = mappingContext.getNodeEntity(mappedRelationship.getEndNodeId());
                if (dirty != null) {
                    LOGGER.debug("flushing end node of: (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(),
                        mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                    mappingContext.removeNodeEntity(dirty, true);
                }

                dirty = mappingContext.getNodeEntity(mappedRelationship.getStartNodeId());
                if (dirty != null) {
                    LOGGER.debug("flushing start node of: (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(),
                        mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                    mappingContext.removeNodeEntity(dirty, true);
                }
            }
        }
    }

    /**
     * Builds Cypher to save the specified object and all its composite objects into the graph database.
     *
     * @param entity The object to persist into the graph database as a node
     * @return The "root" node of the object graph that matches
     */
    private NodeBuilder mapEntity(Object entity, int horizon) {

        // if this object is transient it won't have a classinfo, and isn't persistable
        ClassInfo classInfo = metaData.classInfo(entity);
        if (classInfo == null) {
            return null;
        }

        CompileContext context = compiler.context();
        NodeBuilder nodeBuilder = context.visitedNode(entity);

        if (context.visited(entity, horizon)) {
            LOGGER.debug("already visited: {}", entity);
            return nodeBuilder;
        }

        if (nodeBuilder == null) {
            nodeBuilder = newNodeBuilder(entity, horizon);
            if (!isWriteProtected(WriteProtectionTarget.PROPERTIES, entity)) {
                updateNode(entity, context, nodeBuilder);
            }
        }

        if (horizon != 0) {
            mapEntityReferences(entity, nodeBuilder, horizon - 1);
        } else {
            LOGGER.debug("at horizon 0: {} ", entity);
        }
        return nodeBuilder;
    }

    private boolean isWriteProtected(WriteProtectionTarget mode, Object target) {
        return this.optionalWriteProtectionSupplier.map(supplier -> supplier.apply(mode, target.getClass())) //
            .map(p -> p.test(target)) //
            .orElse(false);
    }

    /**
     * Creates a new node or updates an existing one in the graph, if it has changed.
     *
     * @param entity      the domain object to be persisted
     * @param context     the current {@link CompileContext}
     * @param nodeBuilder a {@link NodeBuilder} that knows how to compile node create/update cypher phrases
     */
    private void updateNode(Object entity, CompileContext context, NodeBuilder nodeBuilder) {
        // fire pre-save event here
        if (mappingContext.isDirty(entity)) {
            LOGGER.debug("{} has changed", entity);
            context.register(entity);
            ClassInfo classInfo = metaData.classInfo(entity);
            updateFieldsOnBuilder(entity, nodeBuilder, classInfo);
        } else {
            context.deregister(nodeBuilder);
            LOGGER.debug("{}, has not changed", entity);
        }

    }

    /**
     * Returns a {@link NodeBuilder} responsible for handling new or updated nodes
     *
     * @param entity  the object to save
     * @param horizon current horizon
     * @return a new {@link NodeBuilder} object for a new node, null for transient classes or subclasses thereof
     */
    private NodeBuilder newNodeBuilder(Object entity, int horizon) {

        ClassInfo classInfo = metaData.classInfo(entity);
        // transient or subclass of transient will not have class info
        if (classInfo == null) {
            return null;
        }

        CompileContext context = compiler.context();

        Long id = mappingContext.nativeId(entity);
        Collection<String> labels = EntityUtils.labels(entity, metaData);

        String primaryIndex = null;
        if (classInfo.hasPrimaryIndexField()) {
            FieldInfo primaryIndexField = classInfo.primaryIndexField();
            primaryIndex = joinPrimaryIndexAttributesIfNecessary(primaryIndexField.property(),
                primaryIndexField.hasCompositeConverter() ? primaryIndexField.readComposite(entity) : null);
        }

        NodeBuilder nodeBuilder;
        if (id < 0) {
            nodeBuilder = compiler.newNode(id).addLabels(labels).setPrimaryIndex(primaryIndex);
            context.registerNewObject(id, entity);
        } else {
            nodeBuilder = compiler.existingNode(id);
            nodeBuilder.addLabels(labels).setPrimaryIndex(primaryIndex);

            this.mappingContext.getSnapshotOf(entity).ifPresent(snapshot ->
                nodeBuilder
                    .setPreviousDynamicLabels(snapshot.getDynamicLabels())
                    .setPreviousCompositeProperties(snapshot.getDynamicCompositeProperties())
            );
        }

        LOGGER.debug("visiting: {}", entity);
        context.visit(entity, nodeBuilder, horizon);

        return nodeBuilder;
    }

    /**
     * Finds all the objects that can be mapped via relationships from the object 'entity' and
     * links them in the graph.
     * This includes objects that are directly linked, as well as objects linked via a relationship entity
     *
     * @param entity      the node whose relationships will be updated
     * @param nodeBuilder a {@link NodeBuilder} that knows how to create node create/update cypher phrases
     * @param horizon     the depth in the tree. If this reaches 0, we stop mapping any deeper
     */
    private void mapEntityReferences(final Object entity, NodeBuilder nodeBuilder, int horizon) {

        int depth = currentDepth.incrementAndGet();
        LOGGER.debug("mapping references declared by: {}, currently at depth {}", entity, depth);

        ClassInfo srcInfo = metaData.classInfo(entity);
        Long srcIdentity = mappingContext.nativeId(entity);

        for (FieldInfo reader : srcInfo.relationshipFields()) {

            String relationshipType = reader.relationshipType();
            Direction relationshipDirection = reader.relationshipDirection();
            Class startNodeType = srcInfo.getUnderlyingClass();
            Class endNodeType = DescriptorMappings.getType(reader.getTypeDescriptor());

            LOGGER.debug("{}: mapping reference type: {}", entity, relationshipType);

            DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType,
                relationshipDirection);

            CompileContext context = compiler.context();

            if (srcIdentity >= 0) {
                List<Class<?>> potentiallyRelated = new ArrayList<>();
                potentiallyRelated.add(endNodeType);

                ClassInfo endNodeTypeClassInfo = metaData.classInfo(endNodeType);
                if (endNodeTypeClassInfo != null) {
                    endNodeTypeClassInfo.allSubclasses()
                        .stream().map(ClassInfo::getUnderlyingClass).forEach(potentiallyRelated::add);
                }

                boolean cleared = false;
                for (Class<?> clazz : potentiallyRelated) {
                    if (clearContextRelationships(context, srcIdentity, clazz, directedRelationship)) {
                        cleared = true;
                    }
                }

                if (!cleared) {
                    LOGGER.debug("this relationship is already being managed: {}-{}-{}-()", entity, relationshipType,
                        relationshipDirection);
                    continue;
                }
            }

            Object relatedObject = reader.read(entity);
            if (relatedObject != null) {

                // if the type of a relationship backed by a relationship entity is not the same as its declared type
                // in the parent object, and the reader on the parent object is abstract, we need to check
                // the directedRelationship object, or the incorrect edge type may be persisted.

                if (isRelationshipEntity(relatedObject)) {
                    ClassInfo declaredObjectInfo = metaData.classInfo(relationshipType);
                    if (declaredObjectInfo.isAbstract()) {
                        final ClassInfo relatedObjectClassInfo = metaData.classInfo(relatedObject);
                        if (!relatedObjectClassInfo.neo4jName().equals(directedRelationship.type())) {
                            directedRelationship = new DirectedRelationship(relatedObjectClassInfo.neo4jName(),
                                directedRelationship.direction());
                            relationshipType = directedRelationship.type();
                        }
                    }
                }

                RelationshipNodes relNodes = new RelationshipNodes(entity, null, startNodeType, endNodeType);
                relNodes.sourceId = srcIdentity;
                Boolean mapBothWays = null;
                for (Object tgtObject : CollectionUtils.iterableOf(relatedObject)) {

                    if (tgtObject == null) {
                        throw new InvalidRelationshipTargetException(startNodeType, relationshipType, reader.getName(),
                            endNodeType);
                    }

                    if (mapBothWays == null) {
                        mapBothWays = bothWayMappingRequired(entity, relationshipType, tgtObject,
                            relationshipDirection);
                    }
                    relNodes.target = tgtObject;
                    link(directedRelationship, nodeBuilder, horizon, mapBothWays, relNodes);
                }
            }
        }

        currentDepth.decrementAndGet();
    }

    /**
     * Clears the relationships in the compiler context for the object represented by identity
     *
     * @param compileContext              the {@link CompileContext} for the current compiler instance
     * @param identity             the id of the node at the the 'start' of the relationship
     * @param endNodeType          the class of the entity on the end of the relationship
     * @param directedRelationship {@link DirectedRelationship} representing the relationships to be cleared
     */
    private boolean clearContextRelationships(CompileContext compileContext, Long identity, Class endNodeType,
        DirectedRelationship directedRelationship) {
        switch (directedRelationship.direction()) {
            case INCOMING:
                LOGGER.debug("context-del: ({})<-[:{}]-()", identity, directedRelationship.type());
                return compileContext.deregisterIncomingRelationships(identity, directedRelationship.type(), endNodeType,
                    metaData.isRelationshipEntity(endNodeType.getName()));

            case OUTGOING:
                LOGGER.debug("context-del: ({})-[:{}]->()", identity, directedRelationship.type());
                return compileContext.deregisterOutgoingRelationships(identity, directedRelationship.type(), endNodeType);

            default:
                //An undirected relationship, clear both directions
                LOGGER.debug("context-del: ({})<-[:{}]-()", identity, directedRelationship.type());
                LOGGER.debug("context-del: ({})-[:{}]->()", identity, directedRelationship.type());
                boolean clearedIncoming = compileContext
                    .deregisterIncomingRelationships(identity, directedRelationship.type(), endNodeType,
                        metaData.isRelationshipEntity(endNodeType.getName()));
                boolean clearedOutgoing = compileContext
                    .deregisterOutgoingRelationships(identity, directedRelationship.type(), endNodeType);
                return clearedIncoming || clearedOutgoing;
        }
    }

    /**
     * Handles the requirement to link two nodes in the graph for the cypher compiler. Either node may or
     * may not already exist in the graph. The nodes at the ends of the relationships are represented
     * by source and target, but the use of these names does not imply any particular direction in the graph.
     * Instead, the direction of the relationship is established between source and target by means of
     * the relationshipDirection argument.
     * In the event that the relationship being managed is represented by an instance of RelationshipEntity
     * then the target will always be a RelationshipEntity, and the actual relationship will be
     * established between the relevant start and end nodes.
     *
     * @param directedRelationship the {@link DirectedRelationship} representing the relationship type and direction
     * @param nodeBuilder          a {@link NodeBuilder} that knows how to create cypher node phrases
     * @param horizon              the current depth we have mapped the domain model to.
     * @param mapBothDirections    whether the nodes should be linked in both directions
     * @param relNodes             {@link EntityGraphMapper.RelationshipNodes} representing the nodes to be linked
     */
    private void link(DirectedRelationship directedRelationship, NodeBuilder nodeBuilder,
        int horizon, boolean mapBothDirections, RelationshipNodes relNodes) {

        LOGGER.debug("linking to entity {} in {} direction", relNodes.target, mapBothDirections ? "both" : "one");

        if (relNodes.target != null) {
            CompileContext context = compiler.context();

            RelationshipBuilder relationshipBuilder = getRelationshipBuilder(compiler, relNodes.target,
                directedRelationship, mapBothDirections);

            if (isRelationshipEntity(relNodes.target)) {
                LOGGER.debug("mapping relationship entity");
                Long reIdentity = mappingContext.nativeId(relNodes.target);
                if (!context.visitedRelationshipEntity(reIdentity)) {
                    mapRelationshipEntity(relNodes.target, relNodes.source, relationshipBuilder, context, nodeBuilder,
                        horizon, relNodes.sourceType, relNodes.targetType);
                } else {
                    LOGGER.debug("RE already visited {}: ", relNodes.target);
                }
            } else {
                LOGGER.debug("mapping related entity");
                mapRelatedEntity(nodeBuilder, relationshipBuilder, currentDepth.get(), horizon, relNodes);
            }
        } else {
            LOGGER.debug("cannot create relationship: ({})-[:{}]->(null)", relNodes.sourceId,
                directedRelationship.type());
        }
    }

    /**
     * Fetches and initialises an appropriate {@link RelationshipBuilder} for the specified relationship type
     * and direction to the supplied domain object, which may be a node or relationship in the graph.
     * In the event that the domain object is a {@link RelationshipEntity}, we create a new relationship, collect
     * its properties and return a builder associated to the RE's end node instead
     *
     * @param cypherBuilder        the {@link org.neo4j.ogm.cypher.compiler.Compiler}
     * @param entity               an object representing a node or relationship entity in the graph
     * @param directedRelationship the {@link DirectedRelationship} representing the relationship type and direction we want to establish
     * @param mapBothDirections    whether the nodes should be linked in both directions
     * @return The appropriate {@link RelationshipBuilder}
     */
    private RelationshipBuilder getRelationshipBuilder(Compiler cypherBuilder, Object entity,
        DirectedRelationship directedRelationship, boolean mapBothDirections) {

        RelationshipBuilder relationshipBuilder;

        if (isRelationshipEntity(entity)) {
            Long relId = mappingContext.nativeId(entity);

            boolean relationshipIsNew = relId < 0;
            boolean relationshipEndsChanged = haveRelationEndsChanged(entity, relId);

            if (relationshipIsNew || relationshipEndsChanged) {
                relationshipBuilder = cypherBuilder.newRelationship(directedRelationship.type());
                if (relationshipEndsChanged) {
                    // since this relationship will get recreated and all properties will be copied in the process,
                    // we have to reset the control fields for version and identifier.
                    FieldInfo versionField = metaData.classInfo(entity).getVersionField();
                    if (versionField != null) {
                        versionField.write(entity, null);
                    }
                    EntityUtils.setIdentity(entity, null, metaData);
                }
            } else {
                relationshipBuilder = cypherBuilder.existingRelationship(relId, directedRelationship.direction(), directedRelationship.type(), mappingContext.isDirty(entity));

                this.mappingContext.getSnapshotOf(entity).ifPresent(snapshot ->
                    relationshipBuilder
                        .setPreviousCompositeProperties(snapshot.getDynamicCompositeProperties())
                );
            }
        } else {
            relationshipBuilder = cypherBuilder.newRelationship(directedRelationship.type(), mapBothDirections);
        }

        relationshipBuilder.direction(directedRelationship.direction());
        if (isRelationshipEntity(entity)) {
            // indicates that this relationship type can be mapped multiple times between 2 nodes
            relationshipBuilder.setSingleton(false);
            relationshipBuilder.setReference(mappingContext.nativeId(entity));
            relationshipBuilder.setRelationshipEntity(true);

            ClassInfo classInfo = metaData.classInfo(entity);
            if (classInfo.primaryIndexField() != null) {
                relationshipBuilder.setPrimaryIdName(classInfo.primaryIndexField().propertyName());
            }
        }
        return relationshipBuilder;
    }

    /**
     * Check if any of the end nodes of the relationship have changed or are new
     *
     * @param entity the relationship entity
     * @param relId  the id of the relationship entity
     * @return true if either end is new or changed
     */
    private boolean haveRelationEndsChanged(Object entity, Long relId) {
        Object startEntity = getStartEntity(metaData.classInfo(entity), entity);
        Object targetEntity = getTargetEntity(metaData.classInfo(entity), entity);

        if (startEntity == null || targetEntity == null) {
            throw new MappingException("Relationship entity " + entity + " cannot have a missing start or end node");
        }
        Long tgtIdentity = mappingContext.nativeId(targetEntity);
        Long srcIdentity = mappingContext.nativeId(startEntity);

        boolean relationshipEndsChanged = false;

        for (MappedRelationship mappedRelationship : mappingContext.getRelationships()) {
            if (mappedRelationship.getRelationshipId() != null && relId != null && mappedRelationship
                .getRelationshipId().equals(relId)) {
                if (srcIdentity == null || tgtIdentity == null || mappedRelationship.getStartNodeId() != srcIdentity
                    || mappedRelationship.getEndNodeId() != tgtIdentity) {
                    relationshipEndsChanged = true;
                    break;
                }
            }
        }
        return relationshipEndsChanged;
    }

    /**
     * Handles the requirement to create or update a relationship in the graph from a domain object
     * that is a {@link RelationshipEntity}. Returns the the object associated with the end node of that
     * relationship in the graph.
     *
     * @param relationshipEntity  the relationship entity to create or update the relationship from
     * @param relationshipBuilder a {@link RelationshipBuilder} that knows how to build cypher phrases about relationships
     * @param context             the {@link CompileContext} for the compiler.
     */
    private void mapRelationshipEntity(Object relationshipEntity, Object parent,
        RelationshipBuilder relationshipBuilder, CompileContext context, NodeBuilder nodeBuilder,
        int horizon, Class startNodeType, Class endNodeType) {

        LOGGER.debug("mapping relationshipEntity {}", relationshipEntity);

        ClassInfo relEntityClassInfo = metaData.classInfo(relationshipEntity);

        // create or update the re's properties
        updateRelationshipEntity(context, relationshipEntity, relationshipBuilder, relEntityClassInfo);

        Object startEntity = getStartEntity(relEntityClassInfo, relationshipEntity);
        Object targetEntity = getTargetEntity(relEntityClassInfo, relationshipEntity);

        Long tgtIdentity = mappingContext.nativeId(targetEntity);
        Long srcIdentity = mappingContext.nativeId(startEntity);

        // create or update the relationship mapping register between the start and end nodes. Note, this
        // merely reflects how we're navigating the object graph at this point, it doesn't reflect the direction
        // of the relationship in the underlying graph - we deal with that later.
        RelationshipNodes relNodes;
        if (parent
            == targetEntity) { //We always created a mapped relationship from the true start node to the end node.
            relNodes = new RelationshipNodes(tgtIdentity, srcIdentity, startNodeType, endNodeType);
        } else {
            relNodes = new RelationshipNodes(srcIdentity, tgtIdentity, startNodeType, endNodeType);
        }

        // TODO : move this to a common function
        if (mappingContext.isDirty(relationshipEntity)) {
            context.register(relationshipEntity);
            if (tgtIdentity >= 0 && srcIdentity >= 0) {
                MappedRelationship mappedRelationship = createMappedRelationship(relationshipBuilder, relNodes);
                if (context.removeRegisteredRelationship(mappedRelationship)) {
                    LOGGER.debug("RE successfully marked for re-writing");
                } else {
                    LOGGER.debug("RE is new");
                }
            }
        } else {
            LOGGER.debug("RE is new or has not changed");
        }

        // finally we continue mapping the object graph, creating/updating the edge in the graph from START->END nodes.
        // If we approached the RE from its END-NODE, we then continue mapping the object graph from the START_NODE,
        // or, if we approached the RE from its START_NODE, we continue mapping the object graph from the END_NODE.
        /*Long startIdentity = EntityUtils.identity(startEntity, metaData);
        Long targetIdentity = EntityUtils.identity(targetEntity, metaData);*/

        NodeBuilder srcNodeBuilder = context.visitedNode(startEntity);
        NodeBuilder tgtNodeBuilder = context.visitedNode(targetEntity);

        if (parent == targetEntity) {  // we approached this RE from its END-NODE during object mapping.
            if (!context.visited(startEntity, horizon)) { // skip if we already visited the START_NODE
                relNodes.source = targetEntity; // set up the nodes to link
                relNodes.target = startEntity;
                mapRelatedEntity(nodeBuilder, relationshipBuilder, currentDepth.get(), horizon, relNodes);
            } else {
                updateRelationship(context, tgtNodeBuilder, srcNodeBuilder, relationshipBuilder, relNodes);
            }
        } else { // we approached this RE from its START_NODE during object mapping.
            if (!context.visited(targetEntity, horizon)) {  // skip if we already visited the END_NODE
                relNodes.source = startEntity;  // set up the nodes to link
                relNodes.target = targetEntity;
                mapRelatedEntity(nodeBuilder, relationshipBuilder, currentDepth.get(), horizon, relNodes);
            } else {
                updateRelationship(context, srcNodeBuilder, tgtNodeBuilder, relationshipBuilder, relNodes);
            }
        }
    }

    private void updateRelationshipEntity(CompileContext context, Object relationshipEntity,
        RelationshipBuilder relationshipBuilder, ClassInfo relEntityClassInfo) {

        Long reIdentity = mappingContext.nativeId(relationshipEntity);
        context.visitRelationshipEntity(reIdentity);

        AnnotationInfo annotation = relEntityClassInfo.annotationsInfo().get(RelationshipEntity.class);
        if (relationshipBuilder.type() == null) {
            relationshipBuilder.setType(annotation.get(RelationshipEntity.TYPE, relEntityClassInfo.name()));
        }

        // if the RE is new, register it in the context so that we can set its ID correctly when it is created,
        if (reIdentity < 0) {
            context.registerNewObject(reIdentity, relationshipEntity);
        }

        updateFieldsOnBuilder(relationshipEntity, relationshipBuilder, relEntityClassInfo);
    }

    private <T> void updateFieldsOnBuilder(Object entity, PropertyContainerBuilder<T> builder, ClassInfo classInfo) {
        for (FieldInfo fieldInfo : classInfo.propertyFields()) {
            if (fieldInfo.isReadOnly()) {
                continue;
            }

            if (fieldInfo.isComposite()) {
                Map<String, ?> properties = fieldInfo.readComposite(entity);
                builder.addCompositeProperties(properties);
            } else if (fieldInfo.isVersionField()) {
                updateVersionField(entity, builder, fieldInfo);
            } else {
                builder.addProperty(fieldInfo.propertyName(), fieldInfo.readProperty(entity));
            }
        }
    }

    private <T> void updateVersionField(Object entity, PropertyContainerBuilder<T> builder, FieldInfo fieldInfo) {
        Long version = (Long) fieldInfo.readProperty(entity);
        builder.setVersionProperty(fieldInfo.propertyName(), version);

        if (version == null) {
            version = 0L;
        } else if (mappingContext.isDirty(entity)) {
            version = version + 1;
        }
        fieldInfo.writeDirect(entity, version);
        builder.addProperty(fieldInfo.propertyName(), version);
    }

    private Object getStartEntity(ClassInfo relEntityClassInfo, Object relationshipEntity) {
        FieldInfo actualStartNodeReader = relEntityClassInfo.getStartNodeReader();
        if (actualStartNodeReader != null) {
            return actualStartNodeReader.read(relationshipEntity);
        }
        throw new RuntimeException("@StartNode of a relationship entity may not be null");
    }

    private Object getTargetEntity(ClassInfo relEntityClassInfo, Object relationshipEntity) {
        FieldInfo actualEndNodeReader = relEntityClassInfo.getEndNodeReader();
        if (actualEndNodeReader != null) {
            return actualEndNodeReader.read(relationshipEntity);
        }
        throw new RuntimeException("@EndNode of a relationship entity may not be null");
    }

    /**
     * This function creates a MappedRelationship that will be added into the cypher context.
     * It is only called when both aNode and bNode are already pre-existing nodes in the graph. The relationship
     * itself may or may not already exist in the graph.
     * aNode and bNode here represent the ends of the relationship, and there is no implied understanding
     * of relationship direction between them. Consequently we need to look at the relationshipBuilder's direction
     * property to determine whether to create a mapping from a->b or from b->a.
     * In the event that the relationshipBuilder's direction is UNDIRECTED, and the relationship pre-exists in the
     * graph, it is critical to return the mapping that represents the existing relationship, or the detection of
     * possibly deleted relationships will fail. If the UNDIRECTED relationship is new however, this is not important,
     * so the default is to always return an OUTGOING relationship: a->b
     *
     * @param relationshipBuilder describes the relationship, type, direction, etc.
     * @param relNodes            {@link EntityGraphMapper.RelationshipNodes} representing the nodes at the end of the relationship
     * @return a mappingContext representing a new or existing relationship between aNode and bNode
     */
    private MappedRelationship createMappedRelationship(RelationshipBuilder relationshipBuilder,
        RelationshipNodes relNodes) {

        boolean isRelationshipEntity = relationshipBuilder.isRelationshipEntity();
        MappedRelationship mappedRelationshipOutgoing = new MappedRelationship(relNodes.sourceId,
            relationshipBuilder.type(), relNodes.targetId, isRelationshipEntity ? relationshipBuilder.reference() : null, relNodes.sourceType,
            relNodes.targetType);
        MappedRelationship mappedRelationshipIncoming = new MappedRelationship(relNodes.targetId,
            relationshipBuilder.type(), relNodes.sourceId, isRelationshipEntity ? relationshipBuilder.reference() : null, relNodes.sourceType,
            relNodes.targetType);
        if (relationshipBuilder.hasDirection(Direction.UNDIRECTED)) {
            if (mappingContext.containsRelationship(mappedRelationshipIncoming)) {
                return mappedRelationshipIncoming;
            }
            return mappedRelationshipOutgoing;
        }

        if (relationshipBuilder.hasDirection(Direction.INCOMING)) {
            return mappedRelationshipIncoming;
        }

        return mappedRelationshipOutgoing;
    }

    /**
     * Attempts to build a simple directed relationship in the graph between
     * two objects represented as srcEntity and tgtEntity. This function recursively calls mapEntity on the
     * target entity first before attempting to create the relationship. In this way, the object graph
     * is traversed in depth-first order, and the relationships between the leaf nodes are created
     * first.
     *
     * @param srcNodeBuilder      a {@link NodeBuilder} that knows how to create cypher phrases about nodes
     * @param relationshipBuilder a {@link RelationshipBuilder} that knows how to create cypher phrases about relationships
     * @param horizon             a value representing how deep we are mapping
     * @param relNodes            {@link EntityGraphMapper.RelationshipNodes} representing the nodes at the end of this relationship
     */
    private void mapRelatedEntity(NodeBuilder srcNodeBuilder,
        RelationshipBuilder relationshipBuilder, int level, int horizon, RelationshipNodes relNodes) {

        // context.visited fails if the class isn't a mapped class, so we have to check this first, even if mapEntity will do it again
        ClassInfo classInfo = metaData.classInfo(relNodes.target);
        if (classInfo == null) {
            return;
        }

        CompileContext context = compiler.context();

        boolean alreadyVisitedNode = context.visited(relNodes.target, horizon);
        boolean selfReferentialUndirectedRelationship = relationshipBuilder.hasDirection(Direction.UNDIRECTED)
            && relNodes.source.getClass() == relNodes.target.getClass();
        boolean relationshipFromExplicitlyMappedObject = level == 1;

        // Map this entity (mapEntity checks whether the entity has been visited before)
        NodeBuilder tgtNodeBuilder = mapEntity(relNodes.target, horizon);
        // Map the relationship only
        // - if the entity hasn't been visited before
        // - or the relationship has a defined direction
        // - or the relationships is defined on an object being explicitly mapped
        if (!alreadyVisitedNode || !selfReferentialUndirectedRelationship || relationshipFromExplicitlyMappedObject) {
            LOGGER.debug("trying to map relationship between {} and {}", relNodes.source, relNodes.target);
            relNodes.targetId = mappingContext.nativeId(relNodes.target);
            updateRelationship(context, srcNodeBuilder, tgtNodeBuilder, relationshipBuilder, relNodes);
        }
    }

    /**
     * Handles the requirement to update a relationship in the graph
     * Two scenarios are handled :
     * 1. one or more of the nodes between the relationship is new.
     * In this case, the relationship will also be new
     * 2. both nodes already exist
     * In the case where the src object and tgt object both exist, we need to find out whether
     * the relationship we're considering was loaded previously, or if it has been created by the user
     * and so has not yet been persisted.
     * If we have seen this relationship before we don't want to ask Neo4j to re-establish
     * it for us as it already exists, so we re-register it in the compile context. Because this relationship
     * was previously deleted from the compile context, but not from the mapping context, this brings both
     * mapping contexts into agreement about the status of this relationship, i.e. it has not changed.
     *
     * @param compileContext             the {@link CompileContext} for the current statement compiler
     * @param srcNodeBuilder      a {@link NodeBuilder} that knows how to create cypher phrases about nodes
     * @param tgtNodeBuilder      a {@link NodeBuilder} that knows how to create cypher phrases about nodes
     * @param relationshipBuilder a {@link RelationshipBuilder} that knows how to create cypher phrases about relationships
     * @param relNodes            {@link EntityGraphMapper.RelationshipNodes} representing the nodes at the ends of this relationship
     */
    private void updateRelationship(CompileContext compileContext, NodeBuilder srcNodeBuilder,
        NodeBuilder tgtNodeBuilder,
        RelationshipBuilder relationshipBuilder, RelationshipNodes relNodes) {

        if (relNodes.targetId == null || relNodes.sourceId == null) {
            maybeCreateRelationship(compileContext, srcNodeBuilder.reference(), relationshipBuilder,
                tgtNodeBuilder.reference(), relNodes.sourceType, relNodes.targetType);
        } else {
            MappedRelationship mappedRelationship = createMappedRelationship(relationshipBuilder, relNodes);
            if (!mappingContext.containsRelationship(mappedRelationship)) {
                maybeCreateRelationship(compileContext, srcNodeBuilder.reference(), relationshipBuilder,
                    tgtNodeBuilder.reference(), relNodes.sourceType, relNodes.targetType);
            } else {
                LOGGER.debug("context-add: ({})-[{}:{}]->({})", mappedRelationship.getStartNodeId(),
                    relationshipBuilder.reference(), mappedRelationship.getRelationshipType(),
                    mappedRelationship.getEndNodeId());
                compileContext.registerRelationship(mappedRelationship);
            }
        }
    }

    /**
     * This function is called when we are certain that the relationship in question does not yet exist
     * in the graph, because one of its start / end nodes is also not in the graph.
     * If this is the first time we have seen this relationship as we walk the object graph, we create
     * a new relationship and register it as a transient relationship in the cypher context.
     * If we come across the same relationship again and the corresponding transient relationship already
     * exists, we simply do nothing.
     * The function checks the relationship creation request to ensure it will be handled correctly. This includes
     * ensuring the correct direction is observed, and that a new relationship (a)-[:TYPE]-(b) is created only
     * once from one of the participating nodes (rather than from both ends).
     *
     * @param context             the current compiler {@link CompileContext}
     * @param src                 the compiler's reference to the domain object representing the start node
     * @param relationshipBuilder a {@link RelationshipBuilder} that knows how to create cypher phrases about relationships
     * @param tgt                 the compiler's reference to the domain object representing the end node
     */
    private void maybeCreateRelationship(CompileContext context, Long src, RelationshipBuilder relationshipBuilder,
        Long tgt, Class srcClass, Class tgtClass) {

        if (hasTransientRelationship(context, src, relationshipBuilder, tgt)) {
            LOGGER.debug("new relationship is already registered");
            if (relationshipBuilder.isBidirectional()) {
                relationshipBuilder.relate(src, tgt);
                context.registerTransientRelationship(new SrcTargetKey(src, tgt),
                    new TransientRelationship(src, relationshipBuilder.reference(), relationshipBuilder.type(), tgt,
                        tgtClass,
                        srcClass)); // we log the new relationship in the opposite direction as part of the transaction context.
            }
            return;
        }

        if (relationshipBuilder.hasDirection(Direction.INCOMING)) {
            //Still create a mapped relationship from src->tgt but we need to reconcile the types too
            //If its a rel entity then we want to rebase the startClass to the @StartNode of the rel entity and the endClass to the rel entity
            if (metaData.isRelationshipEntity(tgtClass.getName())) {
                srcClass = tgtClass;
                String start = metaData.classInfo(tgtClass.getName()).getStartNodeReader().getTypeDescriptor();
                tgtClass = DescriptorMappings.getType(start);
            }
            reallyCreateRelationship(context, tgt, relationshipBuilder, src, tgtClass, srcClass);
        } else {
            reallyCreateRelationship(context, src, relationshipBuilder, tgt, srcClass, tgtClass);
        }
    }

    /**
     * Checks whether a new relationship request of the given type between two specified objects has
     * already been registered. The direction of the relationship is ignored. Returns true if
     * the relationship is already registered, false otherwise.
     *
     * @param ctx                 the current compiler {@link CompileContext}
     * @param src                 the compiler's reference to the domain object representing the start (or end) node
     * @param relationshipBuilder the relationshipBuilder
     * @param tgt                 the compiler's reference to the domain object representing the end (or start) node
     * @return true of a transient relationship already exists, false otherwise
     */
    private boolean hasTransientRelationship(CompileContext ctx, Long src, RelationshipBuilder relationshipBuilder,
        Long tgt) {
        for (Object object : ctx.getTransientRelationships(new SrcTargetKey(src, tgt))) {
            if (object instanceof TransientRelationship) {
                if (((TransientRelationship) object).equals(src, relationshipBuilder, tgt)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Establishes a new relationship creation request with the cypher compiler, and creates a new
     * transient relationship in the new object log.
     *
     * @param ctx        the compiler {@link CompileContext}
     * @param src        the compiler's reference to the domain object representing the start (or end) node
     * @param relBuilder a {@link RelationshipBuilder} that knows how to create cypher phrases about relationships
     * @param tgt        the compiler's reference to the domain object representing the end (or start) node
     */
    private void reallyCreateRelationship(CompileContext ctx, Long src, RelationshipBuilder relBuilder, Long tgt,
        Class srcClass, Class tgtClass) {

        relBuilder.relate(src, tgt);
        LOGGER.debug("context-new: ({})-[{}:{}]->({})", src, relBuilder.reference(), relBuilder.type(), tgt);

        ctx.registerTransientRelationship(new SrcTargetKey(src, tgt),
            new TransientRelationship(src, relBuilder.reference(), relBuilder.type(), tgt, srcClass,
                tgtClass)); // we log the new relationship as part of the transaction context.
    }

    /**
     * Determines whether or not the given object is annotated with <code>RelationshipEntity</code> and thus
     * shouldn't be written to a node. Returns true if the object is so annotated, false otherwise
     *
     * @param potentialRelationshipEntity the domain object to check
     * @return true if the domain object is a RelationshipEntity, false otherwise
     */
    private boolean isRelationshipEntity(Object potentialRelationshipEntity) {
        ClassInfo classInfo = metaData.classInfo(potentialRelationshipEntity);
        return classInfo != null && null != classInfo.annotationsInfo().get(RelationshipEntity.class);
    }

    /**
     * Determines whether or not a two way mapping is required for the relationship.
     * Relationships annotated with either {@link Relationship} direction INCOMING or OUTGOING and defined between two entities of the same type
     * will be considered for a dual mapping.
     * Specifically, if the source and target entity are of the same type, and the related object from the source for relationship type R in direction D
     * is the same as the related object from the target for relationship type R in direction D, then the relationship is mapped both ways.
     *
     * @param srcObject             the domain object representing the start node of the relationship
     * @param relationshipType      the type of the relationship from the srcObject
     * @param tgtObject             the domain object representing the end node of the relationship
     * @param relationshipDirection the direction of the relationship from the srcObject
     * @return true if the relationship should be mapped both ways, false otherwise
     */
    private boolean bothWayMappingRequired(Object srcObject, String relationshipType, Object tgtObject,
        Relationship.Direction relationshipDirection) {
        boolean mapBothWays = false;

        ClassInfo tgtInfo = metaData.classInfo(tgtObject);
        if (tgtInfo == null) {
            LOGGER.warn("Unable to process {} on {}. Check the mapping.", relationshipType, srcObject.getClass());
            // #347. attribute is not a rel ? maybe would be better to change FieldInfo.persistableAsProperty ?
            return false;
        }
        for (FieldInfo tgtRelReader : tgtInfo.relationshipFields()) {
            Direction tgtRelationshipDirection = tgtRelReader.relationshipDirection();
            // The relationship direction must be explicitly incoming or outgoing,
            // the source must have the same relationship type to the target as the target to the source
            // and the source must be related to the target and vice versa in the SAME direction
            if (tgtRelationshipDirection != Direction.UNDIRECTED && tgtRelReader.relationshipType()
                .equals(relationshipType) && relationshipDirection.equals(tgtRelationshipDirection)) {
                Object target = tgtRelReader.read(tgtObject);
                mapBothWays = targetEqualsSource(target, srcObject);
            }

            // We don't need any other field if we already found a match.
            if (mapBothWays) {
                break;
            }
        }
        return mapBothWays;
    }

    /**
     * @param target
     * @param srcObject
     * @return True if the target object or any contained object equals the source object.
     */
    private static boolean targetEqualsSource(Object target, Object srcObject) {

        if (target == null) {
            return false;
        }

        if (target instanceof Iterable) {
            for (Object relatedObject : (Iterable<?>) target) {
                if (relatedObject.equals(srcObject)) {
                    return true;
                }
            }
        }

        if (target.getClass().isArray()) {
            for (Object relatedObject : (Object[]) target) {
                if (relatedObject.equals(srcObject)) {
                    return true;
                }
            }
        }

        return target.equals(srcObject);
    }

    static class RelationshipNodes {

        Long sourceId;
        Long targetId;
        Class sourceType;
        Class targetType;
        Object source;
        Object target;

        RelationshipNodes(Long sourceId, Long targetId, Class sourceType, Class targetType) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        RelationshipNodes(Object source, Object target, Class sourceType, Class targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
            this.source = source;
            this.target = target;
        }

        @Override
        public String toString() {
            return "RelationshipNodes{" +
                "sourceId=" + sourceId +
                ", targetId=" + targetId +
                ", sourceType=" + sourceType +
                ", targetType=" + targetType +
                ", source=" + source +
                ", target=" + target +
                '}';
        }
    }
}
