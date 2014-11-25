package org.neo4j.ogm.mapper;

import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.mapper.cypher.compiler.CypherCompiler;
import org.neo4j.ogm.mapper.cypher.compiler.CypherContext;
import org.neo4j.ogm.mapper.cypher.compiler.NodeBuilder;
import org.neo4j.ogm.mapper.cypher.compiler.SingleStatementBuilder;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.session.MappedRelationship;
import org.neo4j.ogm.session.MappedRelationshipCache;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Implementation of {@link ObjectToCypherMapper} that is driven by an instance of {@link MetaData}.
 */
public class ObjectCypherMapper implements ObjectToCypherMapper {

    private final MetaData metaData;

    // todo: the list of mapped relationships belong in the mapping context
    private final List<MappedRelationship> mappedRelationships;
    private final MappingContext mappingContext;

    /**
     * Constructs a new {@link ObjectCypherMapper} that uses the given {@link MetaData}.
     *
     * @param metaData The {@link MetaData} containing the mapping information
     * @param mappedRelationships The {@link MappedRelationshipCache} containing the relationships loaded in the current session
     */
    public ObjectCypherMapper(MetaData metaData, List<MappedRelationship> mappedRelationships, MappingContext mappingContext) {
        this.metaData = metaData;
        this.mappedRelationships = mappedRelationships;
        this.mappingContext = mappingContext;
    }


    @Override
    public CypherContext mapToCypher(Object toPersist, int horizon) {

        if (toPersist == null) {
            throw new NullPointerException("Cannot map null root object");
        }

        CypherCompiler cypherBuilder = new SingleStatementBuilder();
        CypherContext context = new CypherContext();

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
        if (this.mappedRelationships != null) {
            for (MappedRelationship rel : this.mappedRelationships) {
                //System.out.println("checking previous relationship: ($" + rel.getStartNodeId() + ")-[:" + rel.getRelationshipType() + "]->($" + rel.getEndNodeId() + ")");
                if (!context.contains(new MappedRelationship(rel.getStartNodeId(), rel.getRelationshipType(), rel.getEndNodeId()))) {
                    //System.out.println("deleting previous relationship: ($" + rel.getStartNodeId() + ")-[:" + rel.getRelationshipType() + "]->($" + rel.getEndNodeId() + ")");
                    cypherBuilder.unrelate("$" + rel.getStartNodeId(), rel.getRelationshipType(), "$" + rel.getEndNodeId());
                }
            }
        }
    }

    /**
     * Builds Cypher to save the specified object and all its composite objects into the graph database.
     *
     * @param cypherBuilder The builder used to construct the query
     * @param toPersist The object to persist into the graph database
     * @param context A {@link org.neo4j.ogm.mapper.cypher.compiler.CypherContext} that manages the objects visited during the mapping process
     * @return The "root" node of the object graph that matches
     */
    private NodeBuilder deepMap(CypherCompiler cypherBuilder, Object toPersist, CypherContext context, int horizon) {

        if (context.visited(toPersist)) {
            return context.retrieveNodeBuilderForObject(toPersist);
        }

        ClassInfo classInfo = metaData.classInfo(toPersist.getClass().getName());
        NodeBuilder nodeBuilder = getNodeBuilder(cypherBuilder, toPersist, context);

        // don't give Neo4j more work to do than it needs
        if (mappingContext.isDirty(toPersist, classInfo)) {
             nodeBuilder.mapProperties(toPersist, classInfo);
        }

        if (horizon != 0) {
            mapRelatedObjects(cypherBuilder, toPersist, nodeBuilder, context, horizon - 1);
        }
        return nodeBuilder;
    }

    /**
     * Returns a node-builder responsible for handling new or updated nodes
     *
     * @param cypherBuilder
     * @param toPersist
     * @param context
     * @return
     */
    private NodeBuilder getNodeBuilder(CypherCompiler cypherBuilder, Object toPersist, CypherContext context) {

        ClassInfo classInfo = metaData.classInfo(toPersist.getClass().getName());
        Object id = FieldAccess.read(classInfo.getField(classInfo.identityField()), toPersist);

        if (id == null) {
            NodeBuilder newNode = cypherBuilder.newNode().addLabels(classInfo.labels());
            context.visit(toPersist, newNode);
            context.registerNewObject(newNode.reference(), toPersist);
            return newNode;
        }

        NodeBuilder existingNode = cypherBuilder.existingNode(Long.valueOf(id.toString())).addLabels(classInfo.labels());
        context.visit(toPersist, Long.valueOf(id.toString()), existingNode);


        return existingNode;
    }

    private void mapRelatedObjects(CypherCompiler cypherBuilder, Object toPersist, NodeBuilder source, CypherContext context, int horizon) {

        //System.out.println("mapping relationships from : " + toPersist);

        ClassInfo classInfo = metaData.classInfo(toPersist.getClass().getName());
        Field sourceIdentityField = classInfo.getField(classInfo.identityField());
        Long sourceIdentity = (Long) FieldAccess.read(sourceIdentityField, toPersist);

        for (FieldInfo relField : classInfo.relationshipFields()) {

            Object relatedObject = FieldAccess.read(classInfo.getField(relField), toPersist);
            String relationshipType = resolveRelationshipType(relField);

            if (relatedObject instanceof Iterable) {
                for (Object object : (Iterable<?>) relatedObject) {

                    NodeBuilder target = deepMap(cypherBuilder, object, context, horizon);

                    ClassInfo targetInfo = metaData.classInfo(object.getClass().getName());
                    Field targetIdentityField = targetInfo.getField(targetInfo.identityField());
                    Long targetIdentity = (Long) FieldAccess.read(targetIdentityField, object);

                    if (targetIdentity == null) {
                        //System.out.println("creating new relationship: ($" + sourceIdentity + ")-[:" + relationshipType + "]->(?)");
                        cypherBuilder.relate(source.reference(), relationshipType, target.reference());
                        continue;
                    }

                    MappedRelationship relationship = new MappedRelationship(sourceIdentity, relationshipType, targetIdentity);
                    if (!mappedRelationships.contains(relationship)) {
                        //System.out.println("creating new relationship: ($" + sourceIdentity + ")-[:" + relationshipType + "]->($" + targetIdentity + ")");
                        cypherBuilder.relate(source.reference(), relationshipType, target.reference());
                    } else {
                        //System.out.println("skipping unchanged relationship: ($" + sourceIdentity + ")-[:" + relationshipType + "]->($" + targetIdentity + ")");
                        context.registerRelationship(relationship);
                    }
                }
            } else {
                if (relatedObject != null && !context.visited(toPersist)) {

                    NodeBuilder target = deepMap(cypherBuilder, relatedObject, context, horizon);

                    // TODO: assuming outbound relationship, need to consider what the annotation says
                    ClassInfo targetInfo = metaData.classInfo(relatedObject.getClass().getName());
                    Field targetIdentityField = targetInfo.getField(targetInfo.identityField());
                    Long targetIdentity = (Long) FieldAccess.read(targetIdentityField, relatedObject);

                    if (targetIdentity == null) {
                        //System.out.println("creating new relationship: ($" + sourceIdentity + ")-[:" + relationshipType + "]->(?)");
                        cypherBuilder.relate(source.reference(), relationshipType, target.reference());
                        break;
                    }

                    MappedRelationship relationship = new MappedRelationship(sourceIdentity, relationshipType, targetIdentity);
                    if (!mappedRelationships.contains(relationship)) {
                        //System.out.println("creating new relationship: ($" + sourceIdentity + ")-[:" + relationshipType + "]->($" + targetIdentity + ")");
                        cypherBuilder.relate(source.reference(), relationshipType, target.reference());
                    } else {
                        //System.out.println("skipping unchanged relationship: ($" + sourceIdentity + ")-[:" + relationshipType + "]->($" + targetIdentity + ")");
                        context.registerRelationship(relationship);
                    }
                }
            }
        }
    }

    private static String resolveRelationshipType(FieldInfo relField) {
        // should be doing the opposite of ObjectGraphMapper#setterNameFromRelationshipType, but I don't know at this point
        // whether or not the relationship is read from an annotation and therefore don't know if I shouldn't do any work!

        // @Adam: the relationship() method will return the relationshipType name if its annotated on the relationShipInfo. If not,
        // it currently returns the field type name, e.g. "Wheel", which is not what we ultimately want.

        // todo: fix this for non-annotated fields (and getters)
        return relField.relationship().toUpperCase();
    }

}
