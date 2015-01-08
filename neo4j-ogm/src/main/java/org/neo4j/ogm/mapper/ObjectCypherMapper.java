package org.neo4j.ogm.mapper;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.compiler.CypherCompiler;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.cypher.compiler.NodeBuilder;
import org.neo4j.ogm.cypher.compiler.SingleStatementBuilder;
import org.neo4j.ogm.entityaccess.DefaultEntityAccessStrategy;
import org.neo4j.ogm.entityaccess.EntityAccessStrategy;
import org.neo4j.ogm.entityaccess.RelationalReader;
import org.neo4j.ogm.metadata.MetaData;
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

        CypherCompiler cypherBuilder = new SingleStatementBuilder();
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

        // don't give Neo4j more work to do than it needs
        if (mappingContext.isDirty(toPersist)) {
            context.log(toPersist);
            nodeBuilder.mapProperties(toPersist, classInfo, objectAccessStrategy);
        }

        if (horizon != 0) {
            mapRelatedObjects(cypherBuilder, toPersist, nodeBuilder, context, horizon - 1);
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

            // clear the relationship<s> in the current context for pre-existing objects
            // note: the mappingContext will still have this relationship entry
            if (srcIdentity != null) {
                context.deregisterRelationships(srcIdentity, relationshipType);
            }

            if (relatedObject instanceof Iterable) {
                logger.debug("(collection)");
                for (Object tgtObject : (Iterable<?>) relatedObject) {
                    mapRelatedObject(cypherBuilder, nodeBuilder, srcObject, srcIdentity, relationshipType, relationshipDirection, tgtObject, context, horizon);
                }
            } else {
                if (relatedObject != null && !context.visited(relatedObject)) {
                    Object tgtObject = relatedObject;
                    logger.debug("(singleton)");
                    mapRelatedObject(cypherBuilder, nodeBuilder, srcObject, srcIdentity, relationshipType, relationshipDirection, tgtObject, context, horizon);
                }
            }
        }
    }

    private void mapRelatedObject(CypherCompiler cypherBuilder, NodeBuilder nodeBuilder, Object srcObject, Long srcIdentity, String relationshipType, String relationshipDirection, Object tgtObject, CypherContext context, int horizon) {

        NodeBuilder target = deepMap(cypherBuilder, tgtObject, context, horizon);

        ClassInfo targetInfo = metaData.classInfo(tgtObject.getClass().getName());
        Long tgtIdentity = (Long) objectAccessStrategy.getIdentityPropertyReader(targetInfo).read(tgtObject);

        // this relationship is new, because the src object or tgt object has not yet been persisted
        if (tgtIdentity == null || srcIdentity == null) {
            maybeCreateRelationship(cypherBuilder, context, nodeBuilder.reference(), relationshipType, relationshipDirection, target.reference());
        } else {
            // in the case where the src object and tgt object both exist, we need to find out whether
            // the relationship we're considering was loaded previously, or if it has been created by the user
            // and so has not yet been persisted.
            MappedRelationship relationship = new MappedRelationship(srcIdentity, relationshipType, tgtIdentity);
            if (!mappingContext.isRegisteredRelationship(relationship)) {
                maybeCreateRelationship(cypherBuilder, context, nodeBuilder.reference(), relationshipType, relationshipDirection, target.reference());
            }
            else {
                // we have seen this relationship before and we don't want to ask Neo4j to re-establish
                // it for us as it already exists, so we register it in the tx context. Because this relationship
                // was previously deleted from the tx context, but not from the mapping context, this brings both
                // mapping contexts into agreement about the status of this relationship, i.e. it has not changed.
                context.registerRelationship(relationship);
            }
        }
    }

    // checks the relationship creation request to ensure it will be handled correctly. This includes
    // ensuring the correct direction is observed, and that a relationship with direction BOTH is created only
    // once from one of the participating nodes (rather than from both ends)
    private void maybeCreateRelationship(CypherCompiler cypherBuilder, CypherContext context, String src, String relationshipType, String relationshipDirection, String tgt) {
        if (relationshipDirection.equals(Relationship.BOTH)) {
            if (hasTransientRelationship(context, src, relationshipType, tgt)) {
                return;
            }
            relationshipDirection.equals(Relationship.OUTGOING);
        }
        if (relationshipDirection.equals(Relationship.OUTGOING)) {
            createRelationship(cypherBuilder, context, src, relationshipType, tgt);
        } else {
            createRelationship(cypherBuilder, context, tgt, relationshipType, src);
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
    private void createRelationship(CypherCompiler cypherCompiler, CypherContext ctx, String src, String type, String tgt) {
        cypherCompiler.relate(src, type, tgt);
        ctx.log(new TransientRelationship(src, type, tgt)); // we log the new relationship as part of the transaction context.
    }

}
