package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.graphaware.graphmodel.neo4j.RelationshipModel;
import org.neo4j.ogm.entityaccess.EntityAccessFactory;
import org.neo4j.ogm.mapper.cypher.CypherBuilder;
import org.neo4j.ogm.mapper.cypher.NodeBuilder;
import org.neo4j.ogm.mapper.cypher.TemporaryDummyCypherBuilder;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.factory.ObjectFactory;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Javadoc
 */
public class ObjectGraphMapper implements GraphModelToObjectMapper<GraphModel>, ObjectToCypherMapper {

    private static final MappingContext mappingContext = new MappingContext();

    private final AttributeDictionary attributeDictionary;
    private final ObjectFactory objectFactory;
    private final EntityAccessFactory entityAccessFactory;
    private final Map<Class<?>, List<Object>> typeMap = new HashMap<>();

    private final Class<?> root;

    /**
     * @param type The type of the root object
     * @param objectFactory The {@link ObjectFactory} to use for instantiating types
     * @param entityAccessorFactory To determine how the property values should be mapped to the fields
     * @param attributeDictionary The {@link AttributeDictionary} to look up fields and corresponding graph properties
     */
    public ObjectGraphMapper(Class<?> type, ObjectFactory objectFactory,
            EntityAccessFactory entityAccessorFactory, AttributeDictionary attributeDictionary) {

        this.root = type;
        this.objectFactory = objectFactory;
        this.entityAccessFactory = entityAccessorFactory;
        this.attributeDictionary = attributeDictionary;
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        typeMap.clear();
        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
            return root();
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to Object", e);
        }
    }

    @Override
    public List<String> mapToCypher(Object toPersist) {
        if (toPersist == null) {
            throw new NullPointerException("Cannot map null root object");
        }

        CypherBuilder cypherBuilder = new TemporaryDummyCypherBuilder();
        deepMap(cypherBuilder, toPersist);
        return cypherBuilder.getStatements();
    }

    /**
     * Builds Cypher to save the specified object and all its composite objects into the graph database.
     *
     * @param cypherBuilder The builder used to construct the query
     * @param toPersist The object to persist into the graph database
     * @return The "root" node of the object graph that matches
     */
    private NodeBuilder deepMap(CypherBuilder cypherBuilder, Object toPersist) {
        NodeBuilder nodeBuilder = buildNode(cypherBuilder, toPersist);
        mapObjectFieldsToProperties(toPersist, nodeBuilder);
        mapNestedEntitiesToGraphObjects(cypherBuilder, toPersist, nodeBuilder);
        return nodeBuilder;
    }

    private NodeBuilder buildNode(CypherBuilder cypherBuilder, Object toPersist) {
        Object id = entityAccessFactory.forIdAttributeOfType(toPersist.getClass()).readValue(toPersist);
        if (id == null) {
            // TODO: need to look up labels for new node based on toPersist.getClass()
            return cypherBuilder.newNode();
        }
        return cypherBuilder.existingNode(Long.valueOf(id.toString()));
    }

    private void mapObjectFieldsToProperties(Object toPersist, NodeBuilder nodeBuilder) {
        /*
         * My feeling is still that I prefer the use of a rich field/property representation (like PersistentField) to
         * provide information about property mappings, rather than making String-based lookups in a dictionary.  However,
         * the use of Strings is in keeping with the current ethos so I've gone with this approach, with a view/hope that
         * a more object-oriented implementation will appear in the future.
         */
        for (String attributeName : attributeDictionary.lookUpValueAttributesFromType(toPersist.getClass())) {
            String propertyName = attributeDictionary.lookUpPropertyNameForAttribute(attributeName);
            Object value = entityAccessFactory.forAttributeOfType(attributeName, toPersist.getClass()).readValue(toPersist);
            nodeBuilder.addProperty(propertyName, value);
        }
    }

    private void mapNestedEntitiesToGraphObjects(CypherBuilder cypherBuilder, Object toPersist, NodeBuilder nodeBuilder) {
        for (String attributeName : attributeDictionary.lookUpCompositeEntityAttributesFromType(toPersist.getClass())) {
            String relationshipType = attributeDictionary.lookUpRelationshipTypeForAttribute(attributeName);
            Object nestedEntity = entityAccessFactory.forAttributeOfType(attributeName, toPersist.getClass()).readValue(toPersist);
            if (nestedEntity instanceof Iterable) {
                // create a relationship for each of these nested entities
                for (Object object : (Iterable<?>) nestedEntity) {
                    NodeBuilder newNode = deepMap(cypherBuilder, object);
                    cypherBuilder.relate(nodeBuilder, relationshipType, newNode);
                }
            } else {
                // TODO: another assumption is that it's outbound, for now
                NodeBuilder newNode = deepMap(cypherBuilder, nestedEntity);
                cypherBuilder.relate(nodeBuilder, relationshipType, newNode);
            }
        }
    }

    // required for tests, since the ids of objects are not unique across tests.
    public void reset() {
        mappingContext.clear();
    }

    public Object root() throws Exception {
        List<Object> roots = typeMap.get(root);
        return roots.get(roots.size()-1);
    }

    public void evict(Class<?> type) {
        typeMap.remove(type);
    }

    public List<Object> get(Class<?> clazz) {
        return typeMap.get(clazz);
    }

    public void register(Object object) {
        List<Object> objectList = typeMap.get(object.getClass());
        if (objectList == null) {
            objectList = new ArrayList<>();
            typeMap.put(object.getClass(), objectList);
        }
        objectList.add(object);
    }

    private void mapNodes(GraphModel graphModel) throws Exception {

        for (NodeModel node : graphModel.getNodes()) {

            Object object = mappingContext.get(node.getId());
            if (object == null) { // this is a never before seen object.
                object = objectFactory.instantiateObjectMappedTo(node);
                mappingContext.register(object, node.getId());
                // Note: ASSUMPTION! the object's properties can't change if we've already parsed this previously!
                setProperties(node, object);
            }
            register(object);
        }
    }

    private void mapRelationships(GraphModel graphModel) throws Exception {

        final List<RelationshipModel> vectorRelationships = new ArrayList<>();

        for (RelationshipModel edge : graphModel.getRelationships()) {
            Object parent = mappingContext.get(edge.getStartNode());
            Object child = mappingContext.get(edge.getEndNode());
            if (setValue(parent, child)) {
                evict(child.getClass());
            } else {
                vectorRelationships.add(edge);
            }
        }
        mapOneToMany(vectorRelationships);
    }

    private void mapOneToMany(List<RelationshipModel> vectorRelationships) throws Exception {
        for (RelationshipModel edge : vectorRelationships) {
            Object instance = mappingContext.get(edge.getStartNode());
            Object parameter = mappingContext.get(edge.getEndNode());
            Class<?> type = parameter.getClass();
            if (get(type) != null) {
                entityAccessFactory.forProperty(type.getSimpleName()).setIterable(instance, get(type));
                evict(type); // we've added all instances of type for this object, no point in repeating the effort.
            }
        }
    }

    private void setProperties(NodeModel nodeModel, Object instance) throws Exception {
        for (Property property : nodeModel.getPropertyList()) {
            entityAccessFactory.forProperty((String) property.getKey()).set(instance, property.getValue());
        }
    }

    private boolean setValue(Object instance, Object parameter) throws Exception {
        try {
            entityAccessFactory.forProperty(parameter.getClass().getSimpleName()).setValue(instance, parameter);
            return true;
        } catch (MappingException me) {
            return false;
        }
    }

}
