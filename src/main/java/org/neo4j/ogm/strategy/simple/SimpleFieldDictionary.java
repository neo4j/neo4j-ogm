package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;
import org.neo4j.ogm.metadata.info.DomainInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SimpleFieldDictionary extends FieldDictionary implements AttributeDictionary {


    public SimpleFieldDictionary(DomainInfo domainInfo) {
        super(domainInfo);
    }
    /*
     * The caller is expected to have already provided the parameter "property" in the expectation
     * that a matching field name will be found.
     */
    @Override
    protected Field findScalarField(Object instance, Object parameter, String property) throws MappingException {

        for (Field field: declaredFieldsOn(instance.getClass())) {
            if (field.getName().equals(property)) {
                Type type = field.getGenericType();
                Class clazz = parameter.getClass();
                if (type.equals(clazz) || type.equals(ClassUtils.unbox(clazz))) {
                    return field;
                }
            }
        }
        throw new MappingException("Could not find field: " + property);
    }

    @Override
    protected Field findCollectionField(Object instance, Object parameter, Class elementType, String property) throws MappingException {

        Class<?> clazz = instance.getClass();
        for (Field field : declaredFieldsOn(clazz)) {
            if (field.getName().startsWith(property)) {

                if (field.getType().isArray()) {
                    Object arrayType = ((Iterable<?>)parameter).iterator().next();
                    if ((arrayType.getClass().getSimpleName() + "[]").equals(field.getType().getSimpleName())) {
                        return field;
                    }
                    if (ClassUtils.primitiveArrayName(elementType).equals(field.getType().getName())) {
                        return field;
                    }

                }
                else if (field.getType().getTypeParameters().length > 0) {
                    Class<?> returnType;
                    try {
                        returnType = Class.forName(field.getType().getName());
                    } catch (Exception e) {
                        throw new MappingException(e.getLocalizedMessage());
                    }
                    if (returnType.isAssignableFrom(parameter.getClass())) {  // the best we can do with type erasure
                        return field;
                    }
                }
            }
        }

        throw new MappingException("Could not find collection or array field: " + property);
    }

    @Override
    public Set<String> lookUpCompositeEntityAttributesFromType(Class<?> typeToPersist) {
        Set<String> compositeEntityAttributes = new HashSet<>();
        Set<String> valueAttributes = lookUpValueAttributesFromType(typeToPersist);

        // assumes all fields that aren't mappable to properties are entities
        for (Field field : declaredFieldsOn(typeToPersist)) {
            if (!valueAttributes.contains(field.getName())) {
                compositeEntityAttributes.add(field.getName());
            }
        }
        return compositeEntityAttributes;
    }

    @Override
    public Set<String> lookUpValueAttributesFromType(Class<?> typeToPersist) {
        Set<String> valueAttributes = new HashSet<>();
        for (Field field : declaredFieldsOn(typeToPersist)) {
            if (ClassUtils.mapsToGraphProperty(field.getType())) {
                valueAttributes.add(field.getName());
            }
        }
        return valueAttributes;
    }

    @Override
    public String lookUpRelationshipTypeForAttribute(String attributeName) {
        if (attributeName == null) {
            return null;
        }
        return "HAS_" + attributeName.toUpperCase();
    }

    @Override
    public String lookUpPropertyNameForAttribute(String attributeName) {
        // simple strategy assumes that the node/relationship property name will match the object attribute name
        return attributeName;
    }

    /**
     * Retrieves all declared fields on the specified type and its superclasses, if any.
     *
     * @param type The {@link Class} from which to elicit the fields
     * @return The {@link Field}s on the specified class and its superclasses
     */
    private static Iterable<Field> declaredFieldsOn(Class<?> type) {
        return collectDeclaredFields(new HashSet<Field>(), type);
    }

    private static Collection<Field> collectDeclaredFields(Collection<Field> fields, Class<?> type) {
        for (Field declaredField : type.getDeclaredFields()) {
            fields.add(declaredField);
        }
        if (type.getSuperclass() != null) {
            collectDeclaredFields(fields, type.getSuperclass());
        }
        return fields;
    }

    // guesses the name of a type value, based on the supplied graph attribute
    // the graph attribute can be a node property, e.g. "Name", or a relationship type e.g. "LIKES"
    //
    // A simple attribute e.g. "PrimarySchool" will be mapped to a value "primarySchool"
    //
    // An attribute with elements separated by underscores will have each element processed and then
    // the parts will be elided to a camelCase name. Elements that imply structure, ("HAS", "IS", "A")
    // will be excluded from the mapping, i.e:
    //
    // "HAS_WHEELS"             => "wheels"
    // "IS_A_BRONZE_MEDALLIST"  => "bronzeMedallist"
    // "CHANGED_PLACES_WITH"    => "changedPlacesWith"
    //
    // the FieldInfo class should help here at some point, but its not accessible currently
    @Override
    public String resolveGraphAttribute(String name) {
        StringBuilder sb = new StringBuilder();
        if (name != null && name.length() > 0) {
            if (!name.contains("_")) {
                sb.append(name.substring(0, 1).toLowerCase());
                sb.append(name.substring(1));
            } else {
                String[] parts = name.split("_");
                for (String part : parts) {
                    String test = part.toLowerCase();
                    if ("has|is|a".contains(test)) continue;
                    String resolved = resolveGraphAttribute(test);
                    if (resolved != null) {
                        if (sb.length() > 0) {
                            sb.append(resolved.substring(0, 1).toUpperCase());
                            sb.append(resolved.substring(1));
                        }
                        else {
                            sb.append(resolved);
                        }
                    }
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    @Override
    public String resolveTypeAttribute(String attributeName, Class<?> owningType) {
        /*
         * we ABSOLUTELY need the owning type here because:
         *  - we can't use the value to set instead because it might be null, so can't resolve the class
         *  - the same attribute name could resolve to different types on different instances
         *
         * Also, the caller needs to know whether it's expecting a property name or relationship type or it
         * doesn't know how to use the resultant String.  Returning something like an Attribute object or a
         * PersistentProperty would help but this'd be inconsistent with resolveGraphAttribute(String)! :-S
         */
        if (attributeName == null || owningType == null) {
            return null;
        }

        for (Field field : declaredFieldsOn(owningType)) {
            if (field.getName().equals(attributeName)) {
                return ClassUtils.mapsToGraphProperty(field.getType())
                        ? lookUpPropertyNameForAttribute(attributeName)
                        : lookUpRelationshipTypeForAttribute(attributeName);
            }
        }
        throw new MappingException("Unable to find field matching attribute: " + attributeName + " on " + owningType);
    }

}
