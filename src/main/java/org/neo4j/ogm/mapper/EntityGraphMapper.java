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

package org.neo4j.ogm.mapper;

import java.util.Iterator;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.cypher.compiler.CypherCompiler;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.cypher.compiler.NewBiDirectionalRelationshipBuilder;
import org.neo4j.ogm.cypher.compiler.NodeBuilder;
import org.neo4j.ogm.cypher.compiler.RelationshipBuilder;
import org.neo4j.ogm.cypher.compiler.SingleStatementCypherCompiler;
import org.neo4j.ogm.entityaccess.DefaultEntityAccessStrategy;
import org.neo4j.ogm.entityaccess.EntityAccessStrategy;
import org.neo4j.ogm.entityaccess.PropertyReader;
import org.neo4j.ogm.entityaccess.RelationalReader;
import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.EntityUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.AnnotationInfo;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link EntityToGraphMapper} that is driven by an instance of {@link MetaData}.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class EntityGraphMapper implements EntityToGraphMapper {

    private final Logger logger = LoggerFactory.getLogger(EntityGraphMapper.class);

    private final MetaData metaData;
    private final EntityAccessStrategy entityAccessStrategy;
    private final MappingContext mappingContext;

    /**
     * Constructs a new {@link EntityGraphMapper} that uses the given {@link MetaData}.
     *
     * @param metaData The {@link MetaData} containing the mapping information
     * @param mappingContext The {@link MappingContext} for the current session
     */
    public EntityGraphMapper(MetaData metaData, MappingContext mappingContext) {
        this.metaData = metaData;
        this.mappingContext = mappingContext;
        this.entityAccessStrategy = new DefaultEntityAccessStrategy();
    }

    @Override
    public CypherContext map(Object entity) {
        return map(entity, -1);
    }

    @Override
    public CypherContext map(Object entity, int horizon) {

        if (entity == null) {
            throw new NullPointerException("Cannot map null object");
        }

        CypherCompiler compiler = new SingleStatementCypherCompiler(metaData);

        // add all the relationships we know about. This includes the relationships that
        // won't be modified by the mapping request.
        for (MappedRelationship mappedRelationship : mappingContext.mappedRelationships()) {
            logger.debug("context-init: (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
            compiler.context().registerRelationship(mappedRelationship);
        }

        logger.debug("context initialised with {} relationships", mappingContext.mappedRelationships().size());


        // if the object is a RelationshipEntity, persist it by persisting both the start node and the end node
        // and then ensure the relationship between the two is created or updated as necessary
        if (isRelationshipEntity(entity)) {

            ClassInfo reInfo = metaData.classInfo(entity);

            Object startNode = entityAccessStrategy.getStartNodeReader(reInfo).read(entity);
            if (startNode == null) {
                throw new RuntimeException("@StartNode of relationship entity may not be null");
            }

            Object endNode = entityAccessStrategy.getEndNodeReader(reInfo).read(entity);
            if (endNode == null) {
                throw new RuntimeException("@EndNode of relationship entity may not be null");
            }

            // map both sides as far as the specified horizon
            NodeBuilder startNodeBuilder = mapEntity(startNode, horizon, compiler);
            NodeBuilder endNodeBuilder = mapEntity(endNode, horizon, compiler);

            // create or update the relationship if its not already been visited in the current compile context
            if (!compiler.context().visitedRelationshipEntity(EntityUtils.identity(entity,metaData))) {

                AnnotationInfo annotationInfo = reInfo.annotationsInfo().get(RelationshipEntity.CLASS);
                String relationshipType = annotationInfo.get(RelationshipEntity.TYPE, null);
                DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType, Relationship.OUTGOING);

                RelationshipBuilder relationshipEmitter = getRelationshipBuilder(compiler, entity, directedRelationship, false);

                // 2. create or update the actual relationship (edge) in the graph
                updateRelationshipEntity(compiler.context(), entity, relationshipEmitter, reInfo);

                ClassInfo targetInfo = metaData.classInfo(endNode);
                ClassInfo startInfo = metaData.classInfo(startNode);

                Long srcIdentity = (Long) entityAccessStrategy.getIdentityPropertyReader(startInfo).read(startNode);
                Long tgtIdentity = (Long) entityAccessStrategy.getIdentityPropertyReader(targetInfo).read(endNode);

                RelationshipNodes relNodes = new RelationshipNodes(srcIdentity, tgtIdentity, startNode.getClass(), endNode.getClass());

                // 2. update the fact of the relationship in the compile context
                updateRelationship(compiler.context(), startNodeBuilder, endNodeBuilder, relationshipEmitter, relNodes);
            }
        } else { // not an RE, simply map the entity
            mapEntity(entity, horizon, compiler);
        }

        deleteObsoleteRelationships(compiler);

        return compiler.compile();
    }

    /**
     * Detects object references (including from lists) that have been deleted in the domain.
     * These must be persisted as explicit requests to delete the corresponding relationship in the graph
     *
     * @param compiler the {@link CypherCompiler} instance.
     */
    private void deleteObsoleteRelationships(CypherCompiler compiler) {
        CypherContext context=compiler.context();
        Iterator<MappedRelationship> mappedRelationshipIterator = mappingContext.mappedRelationships().iterator();

        while (mappedRelationshipIterator.hasNext()) {
            MappedRelationship mappedRelationship = mappedRelationshipIterator.next();
            if (!context.removeRegisteredRelationship(mappedRelationship)) {
                logger.debug("context-del: (${})-[{}:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                compiler.unrelate("$" + mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), "$" + mappedRelationship.getEndNodeId(), mappedRelationship.getRelationshipId());
                clearRelatedObjects(mappedRelationship.getStartNodeId());
                mappedRelationshipIterator.remove();
            }
        }
    }

    private void clearRelatedObjects(Long node) {
        for (MappedRelationship mappedRelationship : mappingContext.mappedRelationships()) {
            if (mappedRelationship.getStartNodeId() == node || mappedRelationship.getEndNodeId() == node) {
                Object dirty = mappingContext.getNodeEntity(mappedRelationship.getEndNodeId());
                // forward
                if (dirty != null) {
                    logger.debug("flushing end node of: (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                    mappingContext.deregister(dirty, mappedRelationship.getEndNodeId());
                }
                // reverse
                dirty = mappingContext.getNodeEntity(mappedRelationship.getStartNodeId());
                if (dirty != null) {
                    logger.debug("flushing start node of: (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                    mappingContext.deregister(dirty, mappedRelationship.getStartNodeId());
                }
            }
        }
    }

    /**
     * Builds Cypher to save the specified object and all its composite objects into the graph database.
     *
     * @param compiler The {@link CypherCompiler} used to construct the query
     * @param entity The object to persist into the graph database as a node
     * @return The "root" node of the object graph that matches
     */
    private NodeBuilder mapEntity(Object entity, int horizon, CypherCompiler compiler) {

        // if this object is transient it won't have a classinfo, and isn't persistable
        if (metaData.classInfo(entity) == null) {
            return null;
        }

        CypherContext context=compiler.context();
        Long identity = EntityUtils.identity(entity, metaData);

        if (context.visited(identity)) {
            logger.debug("already visited: {}", entity);
            return context.nodeBuilder(identity);
        }

        NodeBuilder nodeBuilder = getNodeBuilder(compiler, entity);
        if (nodeBuilder != null) {
            updateNode(entity, context, nodeBuilder);
            if (horizon != 0) {
                mapEntityReferences(entity, nodeBuilder, horizon - 1, compiler);
            } else {
                logger.debug("at horizon: {} ", entity);
            }
        }
        return nodeBuilder;
    }

    /**
     * Creates a new node or updates an existing one in the graph, if it has changed.
     *
     * @param entity the domain object to be persisted
     * @param context  the current {@link CypherContext}
     * @param nodeBuilder a {@link NodeBuilder} that knows how to compile node create/update cypher phrases
     */
    private void updateNode(Object entity, CypherContext context, NodeBuilder nodeBuilder) {
        if (mappingContext.isDirty(entity)) {
            logger.debug("{} has changed", entity);
            context.log(entity);
            ClassInfo classInfo = metaData.classInfo(entity);
            nodeBuilder.mapProperties(entity, classInfo, entityAccessStrategy);
        } else {
            logger.debug("{}, has not changed", entity);
        }
    }

    /**
     * Returns a {@link NodeBuilder} responsible for handling new or updated nodes
     *
     * @param compiler the {@link CypherCompiler}
     * @param entity the object to save
     * @return a {@link NodeBuilder} object for either a new node or an existing one
     */
    private NodeBuilder getNodeBuilder(CypherCompiler compiler, Object entity) {

        ClassInfo classInfo = metaData.classInfo(entity);

        // transient or subclass of transient will not have class info
        if (classInfo == null) {
            return null;
        }

        CypherContext context=compiler.context();

        Object id = entityAccessStrategy.getIdentityPropertyReader(classInfo).read(entity);
        NodeBuilder nodeBuilder;
        if (id == null) {
            nodeBuilder = compiler.newNode().addLabels(classInfo.labels());
            context.registerNewObject(nodeBuilder.reference(), entity);
        } else {
            nodeBuilder = compiler.existingNode(Long.valueOf(id.toString())).addLabels(classInfo.labels());
        }
        Long identity = EntityUtils.identity(entity,metaData);
        context.visit(identity, nodeBuilder);
        logger.debug("visiting: {}", entity);
        return nodeBuilder;
    }

    /**
     * Finds all the objects that can be mapped via relationships from the object 'entity' and
     * links them in the graph.
     *
     * This includes objects that are directly linked, as well as objects linked via a relationship entity
     *
     * @param entity  the node whose relationships will be updated
     * @param nodeBuilder a {@link NodeBuilder} that knows how to create node create/update cypher phrases
     * @param horizon the depth in the tree. If this reaches 0, we stop mapping any deeper
     * @param compiler the {@link CypherCompiler}
     */
    private void mapEntityReferences(final Object entity, NodeBuilder nodeBuilder, int horizon, CypherCompiler compiler) {

        logger.debug("mapping references declared by: {} ", entity);

        ClassInfo srcInfo = metaData.classInfo(entity);

        for (RelationalReader reader : entityAccessStrategy.getRelationalReaders(srcInfo)) {

            String relationshipType = reader.relationshipType();
            String relationshipDirection = reader.relationshipDirection();
            Class startNodeType = srcInfo.getUnderlyingClass();
            Class endNodeType = ClassUtils.getType(reader.typeParameterDescriptor());

            DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType, relationshipDirection);

            CypherContext context=compiler.context();
            Long srcIdentity = (Long) entityAccessStrategy.getIdentityPropertyReader(srcInfo).read(entity);

            if (srcIdentity != null) {
                boolean cleared = clearContextRelationships(context, srcIdentity, endNodeType, directedRelationship);
                if (!cleared) {
                    logger.debug("this relationship is already being managed: {}-{}-{}-()", new Object[] {entity, relationshipType, relationshipDirection});
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
                    if (declaredObjectInfo.isAbstract())
                    {
                        final ClassInfo relatedObjectClassInfo = metaData.classInfo(relatedObject);
                        if (!relatedObjectClassInfo.neo4jName().equals(directedRelationship.type())) {
                            directedRelationship = new DirectedRelationship(relatedObjectClassInfo.neo4jName(), directedRelationship.direction());
                            relationshipType = directedRelationship.type();
                        }
                    }
                }

                logger.debug("mapping reference type: {}", relationshipType);

                RelationshipNodes relNodes = new RelationshipNodes(entity,relatedObject,startNodeType,endNodeType);
                relNodes.sourceId = srcIdentity;
                Boolean mapBothWays = null;
                if (relatedObject instanceof Iterable) {
                    for (Object tgtObject : (Iterable<?>) relatedObject) {
                        if(mapBothWays == null) {
                            mapBothWays = bothWayMappingRequired(entity, relationshipType, tgtObject, relationshipDirection);
                        }
                        relNodes.target = tgtObject;
                        link(compiler, directedRelationship, nodeBuilder, horizon, mapBothWays, relNodes);
                    }
                } else if (relatedObject.getClass().isArray()) {
                    for (Object tgtObject : (Object[]) relatedObject) {
                        if(mapBothWays == null) {
                            mapBothWays = bothWayMappingRequired(entity, relationshipType, tgtObject, relationshipDirection);
                        }
                        relNodes.target = tgtObject;
                        link(compiler, directedRelationship, nodeBuilder, horizon, mapBothWays, relNodes);
                    }
                } else {
                    mapBothWays = bothWayMappingRequired(entity, relationshipType, relatedObject, relationshipDirection);
                    link(compiler, directedRelationship, nodeBuilder, horizon, mapBothWays, relNodes);
                }
            }
        }
    }

    /**
     * Clears the relationships in the compiler context for the object represented by identity
     * @param context the {@link CypherContext} for the current compiler instance
     * @param identity the id of the node at the the 'start' of the relationship
     * @param endNodeType the class of the entity on the end of the relationship
     * @param directedRelationship {@link DirectedRelationship} representing the relationships to be cleared
     */
    private boolean clearContextRelationships(CypherContext context, Long identity, Class endNodeType, DirectedRelationship directedRelationship) {
        if (directedRelationship.direction().equals(Relationship.INCOMING)) {
            logger.debug("context-del: ({})<-[:{}]-()", identity, directedRelationship.type());
            return context.deregisterIncomingRelationships(identity, directedRelationship.type(), endNodeType, metaData.isRelationshipEntity(endNodeType.getName()));
        }
        else if (directedRelationship.direction().equals(Relationship.OUTGOING)) {
            logger.debug("context-del: ({})-[:{}]->()", identity, directedRelationship.type());
            return context.deregisterOutgoingRelationships(identity, directedRelationship.type(), endNodeType);
        }
        else {
            //An undirected relationship, clear both directions
            logger.debug("context-del: ({})<-[:{}]-()", identity, directedRelationship.type());
            logger.debug("context-del: ({})-[:{}]->()", identity, directedRelationship.type());
            boolean clearedIncoming =  context.deregisterIncomingRelationships(identity, directedRelationship.type(), endNodeType, metaData.isRelationshipEntity(endNodeType.getName()));
            boolean clearedOutgoing =  context.deregisterOutgoingRelationships(identity, directedRelationship.type(), endNodeType);
            return clearedIncoming || clearedOutgoing;
        }
    }

    /**
     * Handles the requirement to link two nodes in the graph for the cypher compiler. Either node may or
     * may not already exist in the graph. The nodes at the ends of the relationships are represented
     * by source and target, but the use of these names does not imply any particular direction in the graph.
     *
     * Instead, the direction of the relationship is established between source and target by means of
     * the relationshipDirection argument.
     *
     * In the event that the relationship being managed is represented by an instance of RelationshipEntity
     * then the target will always be a RelationshipEntity, and the actual relationship will be
     * established between the relevant start and end nodes.
     * @param cypherCompiler     the {@link CypherCompiler}
     * @param directedRelationship  the {@link DirectedRelationship} representing the relationship type and direction
     * @param nodeBuilder        a {@link NodeBuilder} that knows how to create cypher node phrases
     * @param horizon            the current depth we have mapped the domain model to.
     * @param mapBothDirections  whether the nodes should be linked in both directions
     * @param relNodes          {@link org.neo4j.ogm.mapper.EntityGraphMapper.RelationshipNodes} representing the nodes to be linked
     */
    private void link(CypherCompiler cypherCompiler, DirectedRelationship directedRelationship, NodeBuilder nodeBuilder, int horizon, boolean mapBothDirections, RelationshipNodes relNodes) {

        logger.debug("linking to entity {} in {} direction", relNodes.target, mapBothDirections ? "both" : "one");

        if (relNodes.target != null) {
            CypherContext context = cypherCompiler.context();

            RelationshipBuilder relationshipBuilder = getRelationshipBuilder(cypherCompiler, relNodes.target, directedRelationship, mapBothDirections);

            if (isRelationshipEntity(relNodes.target)) {
                Long reIdentity = EntityUtils.identity(relNodes.target, metaData);
                if (!context.visitedRelationshipEntity(reIdentity)) {
                    mapRelationshipEntity(relNodes.target, relNodes.source, relationshipBuilder, context, nodeBuilder, cypherCompiler, horizon, relNodes.sourceType, relNodes.targetType);
                }
                else {
                    logger.debug("RE already visited {}: ", relNodes.target);
                }
            } else {
                mapRelatedEntity(cypherCompiler, nodeBuilder, relationshipBuilder, horizon, relNodes);
            }
        } else {
            logger.debug("cannot create relationship: ({})-[:{}]->(null)", relNodes.sourceId, directedRelationship.type());
        }
    }

    /**
     * Fetches and initialises an appropriate {@link RelationshipBuilder} for the specified relationship type
     * and direction to the supplied domain object, which may be a node or relationship in the graph.
     *
     * In the event that the domain object is a {@link RelationshipEntity}, we create a new relationship, collect
     * its properties and return a builder associated to the RE's end node instead
     *
     * @param cypherBuilder the {@link CypherCompiler}
     * @param entity  an object representing a node or relationship entity in the graph
     * @param directedRelationship the {@link DirectedRelationship} representing the relationship type and direction we want to establish
     * @param mapBothDirections whether the nodes should be linked in both directions
     * @return The appropriate {@link RelationshipBuilder}
     */
    private RelationshipBuilder getRelationshipBuilder(CypherCompiler cypherBuilder, Object entity, DirectedRelationship directedRelationship, boolean mapBothDirections) {

        RelationshipBuilder relationshipBuilder;

        if (isRelationshipEntity(entity)) {
            Long relId = (Long) entityAccessStrategy.getIdentityPropertyReader(metaData.classInfo(entity)).read(entity);

            boolean relationshipEndsChanged = haveRelationEndsChanged(entity, relId);

            if(relId == null || relationshipEndsChanged) { //if the RE itself is new, or it exists but has one of it's end nodes changed
                relationshipBuilder = cypherBuilder.newRelationship();
            }
            else {
                relationshipBuilder = cypherBuilder.existingRelationship(relId);
            }
            relationshipBuilder.type(directedRelationship.type());
        } else {
            relationshipBuilder = mapBothDirections ? cypherBuilder.newBiDirectionalRelationship().type(directedRelationship.type()) : cypherBuilder.newRelationship().type(directedRelationship.type());
        }

        relationshipBuilder.direction(directedRelationship.direction());
        if (isRelationshipEntity(entity)) {
            relationshipBuilder.setSingleton(false);  // indicates that this relationship type can be mapped multiple times between 2 nodes
        }
        return relationshipBuilder;
    }

    /**
     * Check if any of the end nodes of the relationship have changed or are new
     * @param entity the relationship entity
     * @param relId the id of the relationship entity
     * @return true if either end is new or changed
     */
    private boolean haveRelationEndsChanged(Object entity, Long relId) {
        Object startEntity = getStartEntity(metaData.classInfo(entity), entity);
        Object targetEntity = getTargetEntity(metaData.classInfo(entity), entity);

        if(startEntity==null || targetEntity==null) {
            throw new MappingException("Relationship entity " + entity + " cannot have a missing start or end node");
        }
        ClassInfo targetInfo = metaData.classInfo(targetEntity);
        ClassInfo startInfo = metaData.classInfo(startEntity);
        Long tgtIdentity = (Long) entityAccessStrategy.getIdentityPropertyReader(targetInfo).read(targetEntity);
        Long srcIdentity = (Long) entityAccessStrategy.getIdentityPropertyReader(startInfo).read(startEntity);

        boolean relationshipEndsChanged= false;

        for (MappedRelationship mappedRelationship : mappingContext.mappedRelationships()) {
			if (mappedRelationship.getRelationshipId()!=null && relId!=null && mappedRelationship.getRelationshipId().equals(relId)) {
				if (srcIdentity==null || tgtIdentity==null || mappedRelationship.getStartNodeId() != srcIdentity || mappedRelationship.getEndNodeId() != tgtIdentity) {
					relationshipEndsChanged=true;
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
     * @param relationshipEntity the relationship entity to create or update the relationship from
     * @param relationshipBuilder a {@link RelationshipBuilder} that knows how to build cypher phrases about relationships
     * @param context the {@link CypherContext} for the compiler.
     */
    private void mapRelationshipEntity(Object relationshipEntity, Object parent, RelationshipBuilder relationshipBuilder, CypherContext context, NodeBuilder nodeBuilder, CypherCompiler cypherCompiler, int horizon, Class startNodeType, Class endNodeType) {

        logger.debug("mapping relationshipEntity {}", relationshipEntity);

        ClassInfo relEntityClassInfo = metaData.classInfo(relationshipEntity);

        updateRelationshipEntity(context, relationshipEntity, relationshipBuilder, relEntityClassInfo);

        Object startEntity = getStartEntity(relEntityClassInfo, relationshipEntity);
        Object targetEntity = getTargetEntity(relEntityClassInfo, relationshipEntity);

        ClassInfo targetInfo = metaData.classInfo(targetEntity);
        ClassInfo startInfo = metaData.classInfo(startEntity);

        Long tgtIdentity = (Long) entityAccessStrategy.getIdentityPropertyReader(targetInfo).read(targetEntity);
        Long srcIdentity = (Long) entityAccessStrategy.getIdentityPropertyReader(startInfo).read(startEntity);

        RelationshipNodes relNodes;
        if(parent == targetEntity) { //We always created a mapped relationship from the true start node to the end node.
            relNodes = new RelationshipNodes(tgtIdentity, srcIdentity, startNodeType, endNodeType);
        }
        else {
            relNodes = new RelationshipNodes(srcIdentity, tgtIdentity, startNodeType, endNodeType);
        }

        if (mappingContext.isDirty(relationshipEntity)) {
            context.log(relationshipEntity);
            if (tgtIdentity != null && srcIdentity!=null) {
                MappedRelationship mappedRelationship = createMappedRelationship(relationshipBuilder, relNodes);
                if (mappingContext.mappedRelationships().remove(mappedRelationship)) {
                    logger.debug("RE successfully marked for re-writing");
                } else {
                    logger.debug("RE is new");
                }
            }
        } else {
            logger.debug("RE is new or has not changed");
        }

        Long startIdentity = EntityUtils.identity(startEntity,metaData);
        Long targetIdentity = EntityUtils.identity(targetEntity,metaData);

        NodeBuilder srcNodeBuilder = context.nodeBuilder(startIdentity);
        NodeBuilder tgtNodeBuilder = context.nodeBuilder(targetIdentity);

        if (parent == targetEntity) {
            if(!context.visited(startIdentity)) {
                relNodes.source = targetEntity;
                relNodes.target = startEntity;
                mapRelatedEntity(cypherCompiler, nodeBuilder, relationshipBuilder, horizon, relNodes);
            }
            else {
                updateRelationship(context, tgtNodeBuilder, srcNodeBuilder, relationshipBuilder, relNodes);
            }
        }
        else { //parent=startEntity
            if(!context.visited(targetIdentity)) {
                relNodes.source = startEntity;
                relNodes.target = targetEntity;
                mapRelatedEntity(cypherCompiler, nodeBuilder, relationshipBuilder, horizon, relNodes);
            }
            else {
                updateRelationship(context, srcNodeBuilder, tgtNodeBuilder, relationshipBuilder, relNodes);
            }
        }
    }

    private void updateRelationshipEntity(CypherContext context, Object relationshipEntity, RelationshipBuilder relationshipBuilder, ClassInfo relEntityClassInfo) {

        Long reIdentity = EntityUtils.identity(relationshipEntity, metaData);
        context.visitRelationshipEntity(reIdentity);

        AnnotationInfo annotation = relEntityClassInfo.annotationsInfo().get(RelationshipEntity.CLASS);
        if (relationshipBuilder.getType() == null) {
            relationshipBuilder.type(annotation.get(RelationshipEntity.TYPE, relEntityClassInfo.name()));
        }

        // if the RE is new, register it in the context so that we can set its ID correctly when it is created,
        if (entityAccessStrategy.getIdentityPropertyReader(relEntityClassInfo).read(relationshipEntity) == null) {
            context.registerNewObject(relationshipBuilder.getReference(), relationshipEntity);
        }

        for (PropertyReader propertyReader : entityAccessStrategy.getPropertyReaders(relEntityClassInfo)) {
            relationshipBuilder.addProperty(propertyReader.propertyName(), propertyReader.read(relationshipEntity));
        }
    }

	private Object getStartEntity(ClassInfo relEntityClassInfo, Object relationshipEntity) {
		RelationalReader actualStartNodeReader = entityAccessStrategy.getStartNodeReader(relEntityClassInfo);
		if (actualStartNodeReader != null) {
			return actualStartNodeReader.read(relationshipEntity);
		}
		throw new RuntimeException("@StartNode of a relationship entity may not be null");
	}

	private Object getTargetEntity(ClassInfo relEntityClassInfo, Object relationshipEntity) {
		RelationalReader actualEndNodeReader = entityAccessStrategy.getEndNodeReader(relEntityClassInfo);
		if (actualEndNodeReader != null) {
			return actualEndNodeReader.read(relationshipEntity);
		}
		throw new RuntimeException("@EndNode of a relationship entity may not be null");
	}


    /**
     * This function creates a MappedRelationship that will be added into the cypher context.
     *
     * It is only called when both aNode and bNode are already pre-existing nodes in the graph. The relationship
     * itself may or may not already exist in the graph.
     *
     * aNode and bNode here represent the ends of the relationship, and there is no implied understanding
     * of relationship direction between them. Consequently we need to look at the relationshipBuilder's direction
     * property to determine whether to create a mapping from a->b or from a->b.
     *
     * In the event that the relationshipBuilder's direction is UNDIRECTED, and the relationship pre-exists in the
     * graph, it is critical to return the mapping that represents the existing relationship, or the detection of
     * possibly deleted relationships will fail. If the UNDIRECTED relationship is new however, this is not important,
     * so the default is to always return an OUTGOING relationship: a->b
     *
     * @param relationshipBuilder describes the relationship, type, direction, etc.
     * @param relNodes  {@link org.neo4j.ogm.mapper.EntityGraphMapper.RelationshipNodes} representing the nodes at the end of the relationship
     * @return a mappingContext representing a new or existing relationship between aNode and bNode
     */
    private MappedRelationship createMappedRelationship(RelationshipBuilder relationshipBuilder, RelationshipNodes relNodes) {

        MappedRelationship mappedRelationshipOutgoing = new MappedRelationship(relNodes.sourceId, relationshipBuilder.getType(), relNodes.targetId, relationshipBuilder.getId(), relNodes.sourceType, relNodes.targetType);
        MappedRelationship mappedRelationshipIncoming = new MappedRelationship(relNodes.targetId, relationshipBuilder.getType(), relNodes.sourceId, relationshipBuilder.getId(), relNodes.sourceType, relNodes.targetType);

        if (relationshipBuilder.hasDirection(Relationship.UNDIRECTED)) {
            if (mappingContext.isRegisteredRelationship(mappedRelationshipIncoming)) {
                return mappedRelationshipIncoming;
            }
            return mappedRelationshipOutgoing;
        }

        if (relationshipBuilder.hasDirection(Relationship.INCOMING)) {
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
     * @param compiler the {@link CypherCompiler}
     * @param srcNodeBuilder  a {@link NodeBuilder} that knows how to create cypher phrases about nodes
     * @param relationshipBuilder a {@link RelationshipBuilder} that knows how to create cypher phrases about relationships
     * @param horizon  a value representing how deep we are mapping
     * @param relNodes  {@link org.neo4j.ogm.mapper.EntityGraphMapper.RelationshipNodes} representing the nodes at the end of this relationship
     */
    private void mapRelatedEntity(CypherCompiler compiler, NodeBuilder srcNodeBuilder, RelationshipBuilder relationshipBuilder, int horizon, RelationshipNodes relNodes) {

        NodeBuilder tgtNodeBuilder = mapEntity(relNodes.target, horizon, compiler);

        // tgtNodeBuilder will be null if tgtObject is a transient class, or a subclass of a transient class
        if (tgtNodeBuilder != null) {
            logger.debug("trying to map relationship between {} and {}", relNodes.source, relNodes.target);
            Long tgtIdentity = (Long) entityAccessStrategy.getIdentityPropertyReader(metaData.classInfo(relNodes.target)).read(relNodes.target);
            CypherContext context = compiler.context();
            relNodes.targetId = tgtIdentity;
            updateRelationship(context, srcNodeBuilder, tgtNodeBuilder, relationshipBuilder, relNodes);
        }
    }

    /**
     * Handles the requirement to update a relationship in the graph
     *
     * Two scenarios are handled :
     *
     * 1. one or more of the nodes between the relationship is new.
     * In this case, the relationship will also be new
     *
     * 2. both nodes already exist
     * In the case where the src object and tgt object both exist, we need to find out whether
     * the relationship we're considering was loaded previously, or if it has been created by the user
     * and so has not yet been persisted.
     *
     * If we have seen this relationship before we don't want to ask Neo4j to re-establish
     * it for us as it already exists, so we re-register it in the compile context. Because this relationship
     * was previously deleted from the compile context, but not from the mapping context, this brings both
     * mapping contexts into agreement about the status of this relationship, i.e. it has not changed.
     * @param context  the {@link CypherContext} for the current statement compiler
     * @param srcNodeBuilder  a {@link NodeBuilder} that knows how to create cypher phrases about nodes
     * @param tgtNodeBuilder   a {@link NodeBuilder} that knows how to create cypher phrases about nodes
     * @param relationshipBuilder a {@link RelationshipBuilder} that knows how to create cypher phrases about relationships
     * @param relNodes             {@link org.neo4j.ogm.mapper.EntityGraphMapper.RelationshipNodes} representing the nodes at the ends of this relationship
     */
    private void updateRelationship(CypherContext context, NodeBuilder srcNodeBuilder, NodeBuilder tgtNodeBuilder, RelationshipBuilder relationshipBuilder, RelationshipNodes relNodes) {

        if (relNodes.targetId == null || relNodes.sourceId == null) {
            //
            maybeCreateRelationship(context, srcNodeBuilder.reference(), relationshipBuilder, tgtNodeBuilder.reference(), relNodes.sourceType, relNodes.targetType);
        } else {
            MappedRelationship mappedRelationship = createMappedRelationship(relationshipBuilder, relNodes);
            if (!mappingContext.isRegisteredRelationship(mappedRelationship)) {
                maybeCreateRelationship(context, srcNodeBuilder.reference(), relationshipBuilder, tgtNodeBuilder.reference(), relNodes.sourceType, relNodes.targetType);
            } else {
                logger.debug("context-add: ({})-[{}:{}]->({})", mappedRelationship.getStartNodeId(), relationshipBuilder.getReference(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                mappedRelationship.activate();
                context.registerRelationship(mappedRelationship);
            }
        }

    }
    /**
     * This function is called when we are certain that the relationship in question does not yet exist
     * in the graph, because one of its start / end nodes is also not in the graph.
     *
     * If this is the first time we have seen this relationship as we walk the object graph, we create
     * a new relationship and register it as a transient relationship in the cypher context.
     *
     * If we come across the same relationship again and the corresponding transient relationship already
     * exists, we simply do nothing.
     *
     * The function checks the relationship creation request to ensure it will be handled correctly. This includes
     * ensuring the correct direction is observed, and that a new relationship (a)-[:TYPE]-(b) is created only
     * once from one of the participating nodes (rather than from both ends).
     *
     * @param context the current compiler {@link CypherContext}
     * @param src the compiler's reference to the domain object representing the start node
     * @param relationshipBuilder a {@link RelationshipBuilder} that knows how to create cypher phrases about relationships
     * @param tgt the compiler's reference to the domain object representing the end node
     */
    private void maybeCreateRelationship(CypherContext context, String src, RelationshipBuilder relationshipBuilder, String tgt, Class srcClass, Class tgtClass) {

        //if (hasTransientRelationship(context, src, relationshipBuilder.getType(), tgt)) {
        if (hasTransientRelationship(context, src, relationshipBuilder, tgt)) {
            logger.debug("new relationship is already registered");
            return;
        }

        if (relationshipBuilder.hasDirection(Relationship.INCOMING)) {
            //Still create a mapped relationship from src->tgt but we need to reconcile the types too
            //If its a rel entity then we want to rebase the startClass to the @StartNode of the rel entity and the endClass to the rel entity
            if (metaData.isRelationshipEntity(tgtClass.getName())) {
                srcClass = tgtClass;
                String start = entityAccessStrategy.getStartNodeReader(metaData.classInfo(tgtClass.getName())).typeParameterDescriptor();
                tgtClass = ClassUtils.getType(start);
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
     * @param ctx the current compiler {@link CypherContext}
     * @param src the compiler's reference to the domain object representing the start (or end) node
     * @param relationshipBuilder the relationshipBuilder
     * @param tgt the compiler's reference to the domain object representing the end (or start) node
     * @return true of a transient relationship already exists, false otherwise
     */
    private boolean hasTransientRelationship(CypherContext ctx, String src, RelationshipBuilder relationshipBuilder, String tgt) {
        for (Object object : ctx.log()) {
            if (object instanceof TransientRelationship) {
                if (((TransientRelationship) object).equalsIgnoreDirection(src, relationshipBuilder, tgt)) {
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
     * @param ctx the compiler {@link CypherContext}
     * @param src the compiler's reference to the domain object representing the start (or end) node
     * @param relBuilder a {@link RelationshipBuilder} that knows how to create cypher phrases about relationships
     * @param tgt the compiler's reference to the domain object representing the end (or start) node
     */
    private void reallyCreateRelationship(CypherContext ctx, String src, RelationshipBuilder relBuilder, String tgt, Class srcClass, Class tgtClass) {

        relBuilder.relate(src, tgt);
        logger.debug("context-new: ({})-[{}:{}]->({})", src, relBuilder.getReference(), relBuilder.getType(), tgt);

        if(relBuilder.isNew()) {  //We only want to create or log new relationships
            ctx.log(new TransientRelationship(src, relBuilder.getReference(), relBuilder.getType(), tgt, srcClass, tgtClass)); // we log the new relationship as part of the transaction context.
            if (relBuilder instanceof NewBiDirectionalRelationshipBuilder) {
                ctx.log(new TransientRelationship(tgt, relBuilder.getReference(), relBuilder.getType(), src, tgtClass, srcClass)); // we log the new relationship in the opposite direction as part of the transaction context.
            }
        }
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
        if (classInfo == null) {
            return false;
        }
        return null != classInfo.annotationsInfo().get(RelationshipEntity.CLASS);
    }

    /**
     * Determines whether or not a two way mapping is required for the relationship.
     * Relationships annotated with either {@link Relationship} direction INCOMING or OUTGOING and defined between two entities of the same type
     * will be considered for a dual mapping.
     * Specifically, if the source and target entity are of the same type, and the related object from the source for relationship type R in direction D
     * is the same as the related object from the target for relationship type R in direction D, then the relationship is mapped both ways.
     *
     *
     * @param srcObject the domain object representing the start node of the relationship
     * @param relationshipType the type of the relationship from the srcObject
     * @param tgtObject the domain object representing the end node of the relationship
     * @param relationshipDirection the direction of the relationship from the srcObject
     * @return true if the relationship should be mapped both ways, false otherwise
     */
    private boolean bothWayMappingRequired(Object srcObject, String relationshipType, Object tgtObject, String relationshipDirection) {
        boolean mapBothWays = false;

        if(tgtObject.getClass().equals(srcObject.getClass())) { //Make sure the source and target objects are of the same type
            ClassInfo tgtInfo = metaData.classInfo(tgtObject);
            for (RelationalReader tgtRelReader : entityAccessStrategy.getRelationalReaders(tgtInfo)) {
                String tgtRelationshipDirection = tgtRelReader.relationshipDirection();
                if ((tgtRelationshipDirection.equals(Relationship.OUTGOING) || tgtRelationshipDirection.equals(Relationship.INCOMING)) //The relationship direction must be explicitly incoming or outgoing
                        && tgtRelReader.relationshipType().equals(relationshipType)) { //The source must have the same relationship type to the target as the target to the source
                    //Moreover, the source must be related to the target and vice versa in the SAME direction
                    if (relationshipDirection.equals(tgtRelationshipDirection)) {

                        Object target = tgtRelReader.read(tgtObject);
                        if (target != null) {
                            if (target instanceof Iterable) {
                                for (Object relatedObject : (Iterable<?>) target) {
                                    if (relatedObject.equals(srcObject)) { //the target is mapped to the source as well
                                        mapBothWays = true;
                                    }
                                }
                            } else if (target.getClass().isArray()) {
                                for (Object relatedObject : (Object[]) target) {
                                    if (relatedObject.equals(srcObject)) { //the target is mapped to the source as well
                                        mapBothWays = true;
                                    }
                                }
                            } else {
                                if (target.equals(srcObject)) { //the target is mapped to the source as well
                                    mapBothWays = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return mapBothWays;
    }

    class RelationshipNodes {
        Long sourceId;
        Long targetId;
        Class sourceType;
        Class targetType;
        Object source;
        Object target;

        public RelationshipNodes(Long sourceId, Long targetId, Class sourceType, Class targetType) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        public RelationshipNodes(Object source, Object target, Class sourceType, Class targetType) {
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
