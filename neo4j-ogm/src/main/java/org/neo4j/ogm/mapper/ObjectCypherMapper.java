package org.neo4j.ogm.mapper;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.cypher.compiler.CypherCompiler;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.cypher.compiler.NodeBuilder;
import org.neo4j.ogm.cypher.compiler.RelationshipBuilder;
import org.neo4j.ogm.cypher.compiler.SingleStatementCypherCompiler;
import org.neo4j.ogm.entityaccess.DefaultEntityAccessStrategy;
import org.neo4j.ogm.entityaccess.EntityAccessStrategy;
import org.neo4j.ogm.entityaccess.PropertyReader;
import org.neo4j.ogm.entityaccess.RelationalReader;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.AnnotationInfo;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ObjectToCypherMapper} that is driven by an instance of {@link MetaData}.
 */
public class ObjectCypherMapper implements ObjectToCypherMapper {

    private final Logger logger = LoggerFactory.getLogger(ObjectCypherMapper.class);

    private final MetaData metaData;
    private final EntityAccessStrategy objectAccessStrategy;
    private final MappingContext mappingContext;

    /**
     * Constructs a new {@link ObjectCypherMapper} that uses the given {@link MetaData}.
     *
     * @param metaData The {@link MetaData} containing the mapping information
     * @param mappingContext The {@link MappingContext} for the current session
     */
    public ObjectCypherMapper(MetaData metaData, MappingContext mappingContext) {
        this.metaData = metaData;
        this.mappingContext = mappingContext;
        this.objectAccessStrategy = new DefaultEntityAccessStrategy();
    }


    @Override
    public CypherContext mapToCypher(Object toPersist, int horizon) {

        if (toPersist == null) {
            throw new NullPointerException("Cannot map null root object");
        }

        CypherCompiler cypherBuilder = new SingleStatementCypherCompiler();
        CypherContext context = new CypherContext();

        // add all the relationships we know about:
        context.registeredRelationships().addAll(mappingContext.mappedRelationships());

        deepMap(cypherBuilder, toPersist, context, horizon);
        deleteObsoleteRelationships(cypherBuilder, context);
        context.setStatements(cypherBuilder.getStatements());

        return context;
    }


    @Override
    public CypherContext mapToCypher(Object toPersist) {
        return mapToCypher(toPersist, -1);
    }

    private void deleteObsoleteRelationships(CypherCompiler cypherBuilder, CypherContext context) {

        for (MappedRelationship rel : mappingContext.mappedRelationships()) {
            logger.debug("delete-check relationship: (${})-[:{}]->(${})", rel.getStartNodeId(), rel.getRelationshipType(), rel.getEndNodeId());
            if (!context.isRegisteredRelationship(rel)) {
                logger.debug("not found in tx context! deleting: (${})-[:{}]->(${})", rel.getStartNodeId(), rel.getRelationshipType(), rel.getEndNodeId());
                cypherBuilder.unrelate("$" + rel.getStartNodeId(), rel.getRelationshipType(), "$" + rel.getEndNodeId());
            }
        }
    }

    /**
     * Builds Cypher to save the specified object and all its composite objects into the graph database.
     *
     * @param cypherBuilder The builder used to construct the query
     * @param toPersist The object to persist into the graph database
     * @param context A {@link org.neo4j.ogm.cypher.compiler.CypherContext} that manages the objects visited during the mapping process
     * @return The "root" node of the object graph that matches
     */
    private NodeBuilder deepMap(CypherCompiler cypherBuilder, Object toPersist, CypherContext context, int horizon) {

        if (context.visited(toPersist)) {
            return context.retrieveNodeBuilderForObject(toPersist);
        }

        ClassInfo classInfo = metaData.classInfo(toPersist.getClass().getName());

        NodeBuilder nodeBuilder = getNodeBuilder(cypherBuilder, toPersist, context);

        // skip all transient classes
        if (nodeBuilder != null) {
            // don't give Neo4j more work to do than it needs
            if (mappingContext.isDirty(toPersist)) {
                context.log(toPersist);
                nodeBuilder.mapProperties(toPersist, classInfo, objectAccessStrategy);
            }

            if (horizon != 0) {
                mapRelatedObjects(cypherBuilder, toPersist, nodeBuilder, context, horizon - 1);
            }
        }
        return nodeBuilder;
    }

    /**
     * Returns a node-builder responsible for handling new or updated nodes
     *
     * @param cypherBuilder the compiler class
     * @param toPersist the object to save
     * @param context the context maintained for the lifetime of the transaction
     * @return a node builder object for either a new node or an existing one
     */
    private NodeBuilder getNodeBuilder(CypherCompiler cypherBuilder, Object toPersist, CypherContext context) {

        ClassInfo classInfo = metaData.classInfo(toPersist.getClass().getName());

        // transient or subclass of transient?
        if (classInfo == null) {
            return null;
        }

        Object id = objectAccessStrategy.getIdentityPropertyReader(classInfo).read(toPersist);

        if (id == null) {
            NodeBuilder newNode = cypherBuilder.newNode().addLabels(classInfo.labels());
            context.visit(toPersist, newNode);
            context.registerNewObject(newNode.reference(), toPersist);
            return newNode;
        }

        NodeBuilder existingNode = cypherBuilder.existingNode(Long.valueOf(id.toString())).addLabels(classInfo.labels());
        context.visit(toPersist, existingNode);

        return existingNode;
    }

    private void mapRelatedObjects(CypherCompiler cypherBuilder, Object srcObject, NodeBuilder nodeBuilder, CypherContext context, int horizon) {

        logger.debug("looking for related objects of: {}", srcObject);

        ClassInfo srcInfo = metaData.classInfo(srcObject.getClass().getName());
        Long srcIdentity = (Long) objectAccessStrategy.getIdentityPropertyReader(srcInfo).read(srcObject);

        for (RelationalReader reader : objectAccessStrategy.getRelationalReaders(srcInfo)) {
            Object relatedObject = reader.read(srcObject);
            String relationshipType = reader.relationshipType();
            String relationshipDirection = reader.relationshipDirection();

            // clear the relationship<s> in the current cypher context for pre-existing objects
            // note: the mappingContext will still have this relationship entry
            if (srcIdentity != null) {
                // TODO: this won't be the right relationship type if it's a RelationshipEntity
                context.deregisterRelationships(srcIdentity, relationshipType);
            }

            if (relatedObject instanceof Iterable) {
                logger.debug("(collection)");
                for (Object tgtObject : (Iterable<?>) relatedObject) {
                    final RelationshipBuilder relationship;
                    if (isRelationshipEntity(tgtObject)) {
                        ClassInfo relEntityClassInfo = metaData.classInfo(tgtObject.getClass().getName());
                        Long relId = (Long) objectAccessStrategy.getIdentityPropertyReader(relEntityClassInfo).read(tgtObject);

                        // only if it's a relationship entity and it's got an ID then we need to to an update
                        relationship = relId != null
                                ? cypherBuilder.existingRelationship(relId)
                                : cypherBuilder.newRelationship();

                        AnnotationInfo annotation = relEntityClassInfo.annotationsInfo().get(RelationshipEntity.CLASS);
                        relationship.type(annotation.get(RelationshipEntity.TYPE, relEntityClassInfo.name()));

                        for (PropertyReader propertyReader : objectAccessStrategy.getPropertyReaders(relEntityClassInfo)) {
                            relationship.addProperty(propertyReader.propertyName(), propertyReader.read(tgtObject));
                        }

                        RelationalReader actualEndNodeReader = objectAccessStrategy.getEndNodeReader(relEntityClassInfo);
                        tgtObject = actualEndNodeReader.read(tgtObject);
                    } else {
                        relationship = cypherBuilder.newRelationship().type(relationshipType);
                    }
                    relationship.direction(relationshipDirection);

                    mapRelatedObject(cypherBuilder, nodeBuilder, srcObject, srcIdentity, relationship, tgtObject, context, horizon);
                }
            } else {
                if (relatedObject != null && !context.visited(relatedObject)) {
                    Object tgtObject = relatedObject;
                    logger.debug("(object ref or array)");

                    //TODO: exactly t'same code as for iterable handling above - refactor
                    final RelationshipBuilder relationship;
                    if (isRelationshipEntity(tgtObject)) {
                        ClassInfo relEntityClassInfo = metaData.classInfo(tgtObject.getClass().getName());
                        Long relId = (Long) objectAccessStrategy.getIdentityPropertyReader(relEntityClassInfo).read(tgtObject);

                        // only if it's a relationship entity and it's got an ID then we need to to an update
                        relationship = relId != null
                                ? cypherBuilder.existingRelationship(relId)
                                : cypherBuilder.newRelationship();

                        AnnotationInfo annotation = relEntityClassInfo.annotationsInfo().get(RelationshipEntity.CLASS);
                        relationship.type(annotation.get(RelationshipEntity.TYPE, relEntityClassInfo.name()));

                        for (PropertyReader propertyReader : objectAccessStrategy.getPropertyReaders(relEntityClassInfo)) {
                            relationship.addProperty(propertyReader.propertyName(), propertyReader.read(tgtObject));
                        }

                        RelationalReader actualEndNodeReader = objectAccessStrategy.getEndNodeReader(relEntityClassInfo);
                        tgtObject = actualEndNodeReader.read(tgtObject);
                    } else {
                        relationship = cypherBuilder.newRelationship().type(relationshipType);
                    }
                    relationship.direction(relationshipDirection);

                    mapRelatedObject(cypherBuilder, nodeBuilder, srcObject, srcIdentity, relationship, tgtObject, context, horizon);
                }
            }
        }
    }

    private void mapRelatedObject(CypherCompiler cypherBuilder, NodeBuilder srcNodeBuilder, Object srcObject, Long srcIdentity, RelationshipBuilder relationship, Object tgtObject, CypherContext context, int horizon) {

        NodeBuilder target = deepMap(cypherBuilder, tgtObject, context, horizon);

        // target will be null if tgtObject is a transient class, or a subclass of a transient class
        if (target != null) {
            ClassInfo targetInfo = metaData.classInfo(tgtObject.getClass().getName());
            Long tgtIdentity = (Long) objectAccessStrategy.getIdentityPropertyReader(targetInfo).read(tgtObject);

            // this relationship is new, because the src object or tgt object has not yet been persisted
            if (tgtIdentity == null || srcIdentity == null) {
                maybeCreateRelationship(cypherBuilder, context, srcNodeBuilder.reference(), relationship, target.reference());
            } else {
                // in the case where the src object and tgt object both exist, we need to find out whether
                // the relationship we're considering was loaded previously, or if it has been created by the user
                // and so has not yet been persisted.
                MappedRelationship mappedRelationship = new MappedRelationship(srcIdentity, relationship.getType(), tgtIdentity);
                if (!mappingContext.isRegisteredRelationship(mappedRelationship)) {
                    maybeCreateRelationship(cypherBuilder, context, srcNodeBuilder.reference(), relationship, target.reference());
                } else {
                    // we have seen this relationship before and we don't want to ask Neo4j to re-establish
                    // it for us as it already exists, so we register it in the tx context. Because this relationship
                    // was previously deleted from the tx context, but not from the mapping context, this brings both
                    // mapping contexts into agreement about the status of this relationship, i.e. it has not changed.
                    context.registerRelationship(mappedRelationship);
                }
            }
        }
    }

    /**
     * Checks the relationship creation request to ensure it will be handled correctly. This includes
     * ensuring the correct direction is observed, and that a relationship with direction BOTH is created only
     * once from one of the participating nodes (rather than from both ends).
     */
    private void maybeCreateRelationship(CypherCompiler cypherBuilder, CypherContext context, String src,
            RelationshipBuilder relationship, String tgt) {
        if (relationship.hasDirection(Relationship.BOTH)) {
            if (hasTransientRelationship(context, src, relationship.getType(), tgt)) {
                return;
            }
        }
        if (relationship.hasDirection(Relationship.OUTGOING)) {
            createRelationship(cypherBuilder, context, src, relationship, tgt);
        } else {
            createRelationship(cypherBuilder, context, tgt, relationship, src);
        }
    }

    // checks whether a relationship creation request is already pending between two objects (in any direction)
    private boolean hasTransientRelationship(CypherContext ctx, String src, String type, String tgt) {
        for (Object object : ctx.log()) {
            if (object instanceof TransientRelationship) {
                if (((TransientRelationship) object).equalsIgnoreDirection(src, type, tgt)) {
                    return true;
                }
            }
        }
        return false;
    }

    // establishes a new relationship creation request with the cypher compiler, and logs a new transient relationship
    private void createRelationship(CypherCompiler cypherCompiler, CypherContext ctx, String src, RelationshipBuilder relBuilder, String tgt) {
        relBuilder.relate(src, tgt);
        // here we just set the start and end nodes on RelationshipBuilder to "activate" it
        // TODO: probably needs refactoring, this is not exactly an intuitive design!
        ctx.log(new TransientRelationship(src, relBuilder.getType(), tgt)); // we log the new relationship as part of the transaction context.
    }

    /**
     * Determines whether or not the given object is annotated with <code>RelationshipEntity</code> and thus shouldn't
     * be written to a node.
     */
    private boolean isRelationshipEntity(Object potentialRelationshipEntity) {
        ClassInfo classInfo = metaData.classInfo(potentialRelationshipEntity.getClass().getName());
        if (classInfo == null) {
            return false;
        }
        return null != classInfo.annotationsInfo().get(RelationshipEntity.class.getName());
    }

}
