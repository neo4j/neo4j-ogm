package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.graphaware.graphmodel.neo4j.RelationshipModel;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.entityaccess.MethodAccess;
import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.factory.MetaDataConstructorObjectFactory;
import org.neo4j.ogm.metadata.factory.ObjectFactory;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

import java.util.*;

public class NewObjectGraphMapper implements NewGraphModelToObjectMapper<GraphModel> {

    private static final MappingContext mappingContext = new MappingContext();

    private final ObjectFactory objectFactory;
    private final MetaData metadata;

    private final GraphModelMapper graphModelMapper;

    //private final ClassDictionary classDictionary;
    //private final MethodDictionary methodDictionary;
    //private final FieldDictionary fieldDictionary;

    public NewObjectGraphMapper(String... packages) {
        metadata = new MetaData(packages);
        objectFactory = new MetaDataConstructorObjectFactory(metadata);
        graphModelMapper = new GraphModelMapper();
        //classDictionary = new AnnotatedClassDictionary(metadata);
        //methodDictionary = new AnnotatedMethodDictionary(metadata);
    }

    @Override
    public <T> T load(Class<T> type, GraphModel graphModel)  {
        mappingContext.setRoot(type);
        //graphModelMapper.map(graphModel);

        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
            return type.cast(mappingContext.getRoot());
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to Object", e);
        }
    }

    private void mapNodes(GraphModel graphModel) throws Exception {
        for (NodeModel node : graphModel.getNodes()) {
            Object object = mappingContext.get(node.getId());
            if (object == null) { // object does not yet exist in our domain
                object = objectFactory.instantiateObjectMappedTo(node);
                mappingContext.register(object, node.getId());
                setProperties(node, object);
            }
            register(object);
        }
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
        MethodInfo methodInfo = classInfo.propertySetter("set" + property.getKey().toString());
        if (methodInfo == null) {
            // also, a cache. metadata does not cache
            FieldInfo fieldInfo = classInfo.propertyField(property.getKey().toString());
            if (fieldInfo == null) {
                // log a warning, we don't recognise this property
            } else {
                System.out.println("writing property field: " + fieldInfo.getName() + ", value= " + property.getValue() + ", class=" + property.getValue().getClass());
                FieldAccess.write(classInfo.getField(fieldInfo), instance, property.getValue());
            }
        } else {
            System.out.println("invoking setter: " + methodInfo.getName() + ", value= " + property.getValue() + ", class=" + property.getValue().getClass());
            MethodAccess.write(classInfo.getMethod(methodInfo,ClassUtils.getType(methodInfo.getDescriptor())), instance, property.getValue());
        }
    }

    /**
     * Registers the object as an instance of its type in the typeMap register.
     *
     * At the end of the parse, there should be only one object left in the typeMap: the one
     * that we're returning to the caller.
     *
     * @param object the object to register.
     */
    public void register(Object object) {
        mappingContext.getObjects(object.getClass()).add(object);
    }

    private MethodInfo getMethodInfo(ClassInfo classInfo, String relationshipType, Object parameter) {

        MethodInfo methodInfo;

        // 1st, try to find a method annotated with the relationship type.
        methodInfo = classInfo.relationshipSetter(relationshipType);
        if (methodInfo != null) return methodInfo;

        // 2nd, try to find a "setXXX" method where XXX is derived from the relationship type
        String setterName = setterNameFromRelationshipType(relationshipType);
        methodInfo = classInfo.relationshipSetter(setterName);
        if (methodInfo != null) return methodInfo;

        // 3rd, try to find a single setter that takes the parameter
        List<MethodInfo> methodInfos = classInfo.findSetters(parameter.getClass());
        if (methodInfos.size() == 1) {
            return methodInfos.iterator().next();
        }
        return null;
    }


    private FieldInfo getFieldInfo(ClassInfo classInfo, String relationshipType, Object parameter) {
        FieldInfo fieldInfo;

        // 1st, try to find a field annotated with with relationship type
        fieldInfo = classInfo.relationshipField(relationshipType);
        if (fieldInfo != null) return fieldInfo;

        // 2nd, try to find a "XXX" field name where XXX is derived from the relationship type
        String fieldName = setterNameFromRelationshipType(relationshipType).substring(4);
        fieldInfo = classInfo.relationshipField(fieldName);
        if (fieldInfo != null) return fieldInfo;

        // 3rd, try to find a single setter that takes the parameter
        List<FieldInfo> fieldInfos = classInfo.findFields(parameter.getClass());
        if (fieldInfos.size() == 1) {
            return fieldInfos.iterator().next();
        }

        return null;
    }


    private void mapRelationships(GraphModel graphModel) throws Exception {

        final List<RelationshipModel> vectorRelationships = new ArrayList<>();

        for (RelationshipModel edge : graphModel.getRelationships()) {

            Object parent   = mappingContext.get(edge.getStartNode());
            Object child    = mappingContext.get(edge.getEndNode());

            String relationshipType = edge.getType();
            ClassInfo parentInfo = metadata.classInfo(parent.getClass().getName());

            MethodInfo methodInfo = getMethodInfo(parentInfo, relationshipType, child);
            if (methodInfo != null) {
                System.out.println("writing relationship: " + relationshipType + " using setter");
                MethodAccess.write(parentInfo.getMethod(methodInfo, child.getClass()), parent, child);
                mappingContext.evict(child.getClass());
                continue;
            }

            FieldInfo fieldInfo = getFieldInfo(parentInfo, relationshipType, child);
            if (fieldInfo != null) {
                System.out.println("writing relationship: " + relationshipType + " using field");
                FieldAccess.write(parentInfo.getField(fieldInfo), parent, child);
                mappingContext.evict(child.getClass());
                continue;

            }
            System.out.println(" ** deferring " + child.getClass().getName() + " relationship: " + relationshipType);
            vectorRelationships.add(edge);
        }
        mapOneToMany(vectorRelationships);
    }

    public List<Object> get(Class<?> clazz) {
        return mappingContext.getObjects(clazz);
    }

    private void mapOneToMany(List<RelationshipModel> vectorRelationships) throws Exception {
        for (RelationshipModel edge : vectorRelationships) {
            Object instance = mappingContext.get(edge.getStartNode());
            Object parameter = mappingContext.get(edge.getEndNode());
            Class<?> type = parameter.getClass();

            if (!get(type).isEmpty()) {
                ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
                // todo: we can be a bit cleverer than this. for example, we could try to find
                // an setter taking an array or iterable that is annotated with the relationship type.

                // and if that fails, we can do the same looking for the same kind of setter whose
                // name can be derived from the relationship type

                // finally, we can do this:
                MethodInfo methodInfo = getIterableMethodInfo(classInfo, type);
                if (methodInfo != null) {
                    System.out.println("writing relationship: " + edge.getType() + " using " + methodInfo.getName());
                    // FIX ME: hack here uses List.class, but we should get this back!
                    MethodAccess.write(classInfo.getMethod(methodInfo, ClassUtils.getType(methodInfo.getDescriptor())), instance, get(type));
                    mappingContext.evict(type); // we've added all instances of type for this object, no point in repeating the effort.

                }
                // and if we have no methods, we can revert back to field access
            }
        }
    }

    private MethodInfo getIterableMethodInfo(ClassInfo classInfo, Class<?> parameterType) {
        List<MethodInfo> methodInfos = classInfo.findIterableMethods(parameterType);
        if (methodInfos.size() == 1) {
            return methodInfos.iterator().next();
        }
        // log a warning. multiple methods match this setter signature. We cannot map the value
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


}
