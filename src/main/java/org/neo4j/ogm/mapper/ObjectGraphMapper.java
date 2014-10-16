package org.neo4j.ogm.mapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.graphaware.graphmodel.neo4j.RelationshipModel;
import org.neo4j.ogm.entityaccess.EntityAccessFactory;
import org.neo4j.ogm.mapper.cypher.CypherBuilder;
import org.neo4j.ogm.mapper.cypher.NodeBuilder;
import org.neo4j.ogm.mapper.cypher.TemporaryDummyCypherBuilder;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.ObjectFactory;

/**
 * TODO: Javadoc
 */
public class ObjectGraphMapper implements GraphModelToObjectMapper<GraphModel>, ObjectToCypherMapper {

    private static final MappingContext mappingContext = new MappingContext();

    private final ObjectFactory objectFactory;
    private final EntityAccessFactory entityAccessFactory;
    private final Map<Class<?>, List<Object>> typeMap = new HashMap<>();

    private final Class<?> root;

    /**
     * @param type The type of the root object
     * @param objectFactory The {@link ObjectFactory} to use for instantiating types
     * @param entityAccessorFactory To determine how the property values should be mapped to the fields
     */
    public ObjectGraphMapper(Class<?> type, ObjectFactory objectFactory,
            EntityAccessFactory entityAccessorFactory) {

        this.root = type;
        this.objectFactory = objectFactory;
        this.entityAccessFactory = entityAccessorFactory;
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        typeMap.clear();
        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
            return root();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> mapToCypher(Object toPersist) {
        if (toPersist == null) {
            throw new NullPointerException("Cannot map null root object");
        }

        /*
         * We don't want to add logic like "am I an ID field?" to EntityAccess, because it's just an entity access mechanism,
         * so we therefore need some kind of dictionary from which to look up the interesting fields.
         *
         * This leaves us with a problem, though, in that this code needs to make a distinction between interesting fields
         * and plain old property fields.  I'd like to see something like a PersistentField because it's much more OO than
         * asking questions of various dictionaries, but it's probably more in keeping with the current ethos if we just
         * use a String for everything.  I'd still prefer to do what's in the playground, FWIW.
         */

        CypherBuilder cypherBuilder = new TemporaryDummyCypherBuilder();
        deepMap(cypherBuilder, toPersist);
        return cypherBuilder.getStatements();
    }

    private NodeBuilder deepMap(CypherBuilder cypherBuilder, Object toPersist) {
        NodeBuilder nodeBuilder = beginNodeClause(cypherBuilder, toPersist);

        // map fields
        Set<String> propertyFieldsToMap = lookUpPropertyFieldsToMapFromType(toPersist.getClass());
        for (String fieldName : propertyFieldsToMap) {
            String propertyName = lookUpPropertyNameFromFieldName(fieldName);
            Object value = entityAccessFactory.forProperty(propertyName).readValue(toPersist);
            nodeBuilder.addProperty(propertyName, value);
        }

        // map nested composite entities
        Set<String> objectsToRecurseInto = lookUpFieldsRepresentingCompositeEntity(toPersist.getClass());
        for (String fieldName : objectsToRecurseInto) {
            String relationshipType = lookUpRelationshipNameForField(fieldName);
            Object nestedEntity = entityAccessFactory.forProperty(lookUpPropertyNameFromFieldName(fieldName)).readValue(toPersist);
            if (nestedEntity instanceof java.lang.Iterable) {
                // create a relationship for each of these nested entities
                for (Object object : (Iterable<?>) nestedEntity) {
                    NodeBuilder newNode = deepMap(cypherBuilder, object);
                    cypherBuilder.relate(nodeBuilder, relationshipType, newNode);
                }
            }
            else {
                // TODO: another assumption is that it's outbound, for now
                NodeBuilder newNode = deepMap(cypherBuilder, nestedEntity);
                cypherBuilder.relate(nodeBuilder, relationshipType, newNode);
            }
        }
        return nodeBuilder;
    }

    private NodeBuilder beginNodeClause(CypherBuilder cypherBuilder, Object toPersist) {
        // XXX: this is the "simple" way, needs a strategy class
        Object id = entityAccessFactory.forProperty("id").readValue(toPersist);
        if (id == null) {
            // TODO: look up labels for new node based on toPersist.getClass()
            return cypherBuilder.newNode();
        }
        return cypherBuilder.existingNode(Long.valueOf(id.toString()));
    }

    private String lookUpRelationshipNameForField(String fieldName) {
        return fieldName.toUpperCase();
    }

    private Set<String> lookUpFieldsRepresentingCompositeEntity(Class<?> typeToPersist) {
        Set<String> propertyFields = lookUpPropertyFieldsToMapFromType(typeToPersist);
        Set<String> nonPropertyFields = new HashSet<>();
        for (Field field : typeToPersist.getDeclaredFields()) {
            if (!propertyFields.contains(field.getName())) {
                nonPropertyFields.add(field.getName());
            }
        }
        return nonPropertyFields;
    }

    private String lookUpPropertyNameFromFieldName(String fieldName) {
        return fieldName;
    }

    private Set<String> lookUpPropertyFieldsToMapFromType(Class<?> typeToPersist) {
        Set<String> fieldNames = new HashSet<>();
        for (Field declaredField : typeToPersist.getDeclaredFields()) {
            // XXX: hacked for now
            if (declaredField.getType().isPrimitive() || declaredField.getType().isArray()
                    || Number.class.isAssignableFrom(declaredField.getType())
                    || declaredField.getType().equals(String.class)) {
                fieldNames.add(declaredField.getName());
            }
        }
        return fieldNames;
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
                object = this.objectFactory.instantiateObjectMappedTo(node);
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
