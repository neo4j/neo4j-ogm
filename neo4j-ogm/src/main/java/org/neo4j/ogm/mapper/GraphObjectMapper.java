package org.neo4j.ogm.mapper;

import org.neo4j.graphmodel.GraphModel;
import org.neo4j.graphmodel.NodeModel;
import org.neo4j.graphmodel.Property;
import org.neo4j.graphmodel.RelationshipModel;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.entityaccess.MethodAccess;
import org.neo4j.ogm.entityaccess.ObjectFactory;
import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

import java.util.*;

public class GraphObjectMapper implements GraphToObjectMapper<GraphModel> {

    private final MappingContext mappingContext;
    private final ObjectFactory objectFactory;
    private final MetaData metadata;

    public GraphObjectMapper(MetaData metaData, MappingContext mappingContext) {
        this.metadata = metaData;
        this.objectFactory = new ObjectFactory(metadata);
        this.mappingContext = mappingContext;
    }

    @Override
    public <T> T load(Class<T> type, GraphModel graphModel)  {
        map(type, graphModel);
        try {
            return type.cast(mappingContext.getRoot(type));
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to instance of " + type.getName(), e);
        }
    }

    @Override
    public <T> Set<T> loadAll(Class<T> type, GraphModel graphModel) {
        map(type, graphModel);
        try {
            Set<T> set = new HashSet<>();
            for (Object o : mappingContext.getObjects(type)) {
                set.add(type.cast(o));
            }
            return set;
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to instance of " + type.getName(), e);
        }
    }

    private <T> void map(Class<T> type, GraphModel graphModel) {
        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to instance of " + type.getName(), e);
        }
    }

    private void mapNodes(GraphModel graphModel) throws Exception {
        for (NodeModel node : graphModel.getNodes()) {
            Object object = mappingContext.get(node.getId());
            if (object == null) {
                object = mappingContext.register(objectFactory.newObject(node), node.getId());
                if (getIdentity(object) == null) {
                    synchronized (object) {
                        setIdentity(object, node.getId());
                        setProperties(node, object);
                        mappingContext.remember(object, metadata.classInfo(object.getClass().getName()));
                    }
                }
            }
        }
    }

    private void setIdentity(Object instance, Long id) throws Exception {
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        FieldInfo fieldInfo = classInfo.identityField();
        FieldAccess.write(classInfo.getField(fieldInfo), instance, id);
    }

    private Long getIdentity(Object instance) throws Exception {
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        FieldInfo fieldInfo = classInfo.identityField();
        return (Long) FieldAccess.read(classInfo.getField(fieldInfo), instance);
    }
    /*
     * by preference we use a setter. if one doesn't exist, fall back to field access
     */
    private void setProperties(NodeModel nodeModel, Object instance) throws Exception {
        // cache this.
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        for (Property property : nodeModel.getPropertyList()) {
            writeProperty(classInfo, instance, property);
        }
    }

    private void writeProperty(ClassInfo classInfo, Object instance, Property property) {

        // this is messy. we need a dictionary to help

        // a cache would help here.
        MethodInfo methodInfo = classInfo.propertySetter(property.getKey().toString());

        if (methodInfo == null) {
            // also, a cache. metadata does not cache
            FieldInfo fieldInfo = classInfo.propertyField(property.getKey().toString());
            if (fieldInfo == null) {
                // log a warning, we don't recognise this property
            } else {
                //LOGGER.info("writing property field: " + fieldInfo.getName() + ", value= " + property.getValue() + ", class=" + property.getValue().getClass());
                FieldAccess.write(classInfo.getField(fieldInfo), instance, property.getValue());
            }
        } else {
            //LOGGER.info("invoking setter: " + methodInfo.getName() + ", value= " + property.getValue() + ", class=" + property.getValue().getClass());
            MethodAccess.write(classInfo.getMethod(methodInfo,ClassUtils.getType(methodInfo.getDescriptor())), instance, property.getValue());
        }
    }

    private MethodInfo getOneToOneRelationshipMethodInfo(ClassInfo classInfo, String relationshipType, Object parameter) {

        MethodInfo methodInfo;

        // try to find a method called or annotated as the relationship type.
        methodInfo = classInfo.relationshipSetter(relationshipType);
        if (methodInfo != null) {
            return methodInfo;
        }

        // try to find a single setter that takes the parameter
        List<MethodInfo> methodInfos = classInfo.findSetters(parameter.getClass());
        if (methodInfos.size() == 1) {
            return methodInfos.iterator().next();
        }
        return null;
    }


    private FieldInfo getOneToOneRelationshipFieldInfo(ClassInfo classInfo, String relationshipType, Object parameter) {
        FieldInfo fieldInfo;

        // try to find a field called or annotated as the neo4j relationship type
        fieldInfo = classInfo.relationshipField(relationshipType);
        if (fieldInfo != null && fieldInfo.isTypeOf(parameter.getClass())) {
            return fieldInfo;
        }

        // try to find a unique field of the parameter class
        List<FieldInfo> fieldInfos = classInfo.findFields(parameter.getClass());
        if (fieldInfos.size() == 1) {
            return fieldInfos.iterator().next();
        }

        return null;
    }

    // todo: on a successful mapping, create and remember a mapped relationship
    private boolean mapOneToOne(Object source, Object target, RelationshipModel edge) {

        String edgeLabel = edge.getType();
        ClassInfo sourceInfo = metadata.classInfo(source.getClass().getName());

        MethodInfo methodInfo = getOneToOneRelationshipMethodInfo(sourceInfo, edgeLabel, target);
        if (methodInfo != null) {
            MethodAccess.write(sourceInfo.getMethod(methodInfo, target.getClass()), source, target);
            String relType = relationshipType(methodInfo.relationship().substring(3));
            mappingContext.remember(new MappedRelationship(edge.getStartNode(), relType, edge.getEndNode()));
            return true;
        }

        FieldInfo fieldInfo = getOneToOneRelationshipFieldInfo(sourceInfo, edgeLabel, target);
        if (fieldInfo != null) {
            FieldAccess.write(sourceInfo.getField(fieldInfo), source, target);
            String relType = relationshipType(fieldInfo.relationship());
            mappingContext.remember(new MappedRelationship(edge.getStartNode(), relType, edge.getEndNode()));
            return true;
        }
        return false;
    }

    private void mapRelationships(GraphModel graphModel) throws Exception {

        final Set<RelationshipModel> oneToMany = new HashSet<>();

        for (RelationshipModel edge : graphModel.getRelationships()) {
            Object source   = mappingContext.get(edge.getStartNode());
            Object target    = mappingContext.get(edge.getEndNode());

            if (!mapOneToOne(source, target, edge)) {
                oneToMany.add(edge);
            }
            mapOneToOne(target, source, edge);  // try the inverse mapping
        }
        mapOneToMany(oneToMany);
    }

    public Set<Object> get(Class<?> clazz) {
        return mappingContext.getObjects(clazz);
    }

    // FIXME: this code is buggy. it is setting the same collection multiple times!
    private void mapOneToMany(Set<RelationshipModel> oneToManyRelationships) throws Exception {

        Map< Object, Map<Class, Set<Object>>> typeRelationships = new HashMap<>();

        // first, build the full set of related objects of each type for each source object in the relationship
        for (RelationshipModel edge : oneToManyRelationships) {

            Object instance = mappingContext.get(edge.getStartNode());
            Object parameter = mappingContext.get(edge.getEndNode());

            Map<Class, Set<Object>> handled = typeRelationships.get(instance);
            if (handled == null) {
                typeRelationships.put(instance, handled=new HashMap<>());
            }
            Class<?> type = parameter.getClass();
            Set<Object> objects = handled.get(type);
            if (objects == null) {
                handled.put(type, objects=new HashSet<>());
            }
            objects.add(parameter);
        }

        // then set the entire collection at the same time.
        for (Object instance : typeRelationships.keySet()) {
            Map<Class, Set<Object>> handled = typeRelationships.get(instance);
            for (Class type : handled.keySet()) {
                Collection objects = handled.get(type);
                mapOneToMany(instance, type, objects, oneToManyRelationships);
            }
        }
    }

    private boolean mapOneToMany(Object instance, Class type, Collection objects, Set<RelationshipModel> edges) {

        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());

        MethodInfo methodInfo = getIterableMethodInfo(classInfo, type);
        if (methodInfo != null) {
            String relType = relationshipType(methodInfo.relationship().substring(3));
            MethodAccess.write(classInfo.getMethod(methodInfo, ClassUtils.getType(methodInfo.getDescriptor())), instance, objects);
            for (RelationshipModel edge : edges) {
                mappingContext.remember(new MappedRelationship(edge.getStartNode(), relType, edge.getEndNode()));
            }
            return true;
        }

        FieldInfo fieldInfo = getIterableFieldInfo(classInfo, type);
        if (fieldInfo != null) {
            String relType = relationshipType(fieldInfo.relationship());
            FieldAccess.write(classInfo.getField(fieldInfo), instance, objects);
            for (RelationshipModel edge : edges) {
                mappingContext.remember(new MappedRelationship(edge.getStartNode(), relType, edge.getEndNode()));
            }
            return true;
        }

        return false;
    }

    private MethodInfo getIterableMethodInfo(ClassInfo classInfo, Class<?> parameterType) {
        List<MethodInfo> methodInfos = classInfo.findIterableSetters(parameterType);
        if (methodInfos.size() == 1) {
            return methodInfos.iterator().next();
        }
        // log a warning. multiple methods match this setter signature. We cannot map the value
        return null;
    }

    private FieldInfo getIterableFieldInfo(ClassInfo classInfo, Class<?> parameterType) {
        List<FieldInfo> fieldInfos = classInfo.findIterableFields(parameterType);
        if (fieldInfos.size() == 1) {
            return fieldInfos.iterator().next();
        }
        // log a warning. multiple fields match this signature. We cannot map the value
        return null;
    }

    // guesses the name of a type accessor method, based on the supplied graph attribute
    // the graph attribute can be a node property, e.g. "Name", or a relationship type e.g. "LIKES"
    //
    // A simple attribute e.g. "PrimarySchool" will be mapped to a value "[get,set]PrimarySchool"
    //
    // An attribute with elements separated by underscores will have each element processed and then
    // the parts will be elided to a camelCase name. Elements that imply structure, ("HAS", "IS", "A")
    // will be excluded from the mapping, i.e:
    //
    // "HAS_WHEELS"             => "[get,set]Wheels"
    // "IS_A_BRONZE_MEDALLIST"  => "[get,set]BronzeMedallist"
    // "CHANGED_PLACES_WITH"    => "[get,set]ChangedPlacesWith"
    //
    // the MethodInfo class should help here at some point, but its not accessible currently
    public String setterNameFromRelationshipType(String name) {
        StringBuilder sb = new StringBuilder();
        if (name != null && name.length() > 0) {
            sb.append("set");
            if (!name.contains("_")) {
                sb.append(name.substring(0, 1).toUpperCase());
                sb.append(name.substring(1));
            } else {
                String[] parts = name.split("_");
                for (String part : parts) {
                    String test = part.toLowerCase();
                    if ("has|is|a".contains(test)) continue;
                    String resolved = setterNameFromRelationshipType(test);
                    if (resolved != null) {
                        sb.append(resolved);
                    }
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    // this is temporary - will be replaced by work Adam is doing.
    private String relationshipType(String property) {
        StringBuilder sb = new StringBuilder();
        for (char c : property.toCharArray()) {
            if (sb.length() > 0 && Character.isUpperCase(c)) {
                sb.append("_");
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }
}
