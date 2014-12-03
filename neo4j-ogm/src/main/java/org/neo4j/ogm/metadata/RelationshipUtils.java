package org.neo4j.ogm.metadata;

/**
 * Contains helper methods to facilitate inference of relationship types from field and methods and vice versa.
 * <p>
 * All methods follow the same convention that relationship types are UPPER_SNAKE_CASE and that fields appear in
 * lowerCamelCase.  The de-facto Java Bean getter/setter pattern is also assumed when inferring accessor methods.
 * </p>
 * The utility methods here will all throw a <code>NullPointerException</code> if invoked with <code>null</code>.
 */
public class RelationshipUtils {

    /**
     * Infers the relationship type that corresponds to the given field or access method name.
     *
     * @param memberName The member name from which to infer the relationship type
     * @return The resolved relationship type
     */
    public static String inferRelationshipType(String memberName) {
        // TODO: improve to change argument from UpperCamelCase to SNAKE_CASE
        if (memberName.startsWith("get") || memberName.startsWith("set")) {
            return memberName.substring(3).toUpperCase();
        }
        return memberName.toUpperCase();
    }

    /**
     * Infers the name of the setter method that corresponds to the given relationship type.
     *
     * @param relationshipType The relationship type from which to infer the setter name
     * @return The inferred setter method name
     */
    public static String inferSetterName(String relationshipType) {
        StringBuilder setterName = toUpperCamelCase(new StringBuilder("set"), relationshipType);
        return setterName.toString();
    }

    /**
     * Infers the name of the getter method that corresponds to the given relationship type.
     *
     * @param relationshipType The relationship type from which to infer the getter name
     * @return The inferred getter method name
     */
    public static String inferGetterName(String relationshipType) {
        StringBuilder getterName = toUpperCamelCase(new StringBuilder("get"), relationshipType);
        return getterName.toString();
    }

    /**
     * Infers the name of the instance variable that corresponds to the given relationship type.
     *
     * @param relationshipType The relationship type from which to infer the name of the field
     * @return The inferred field name
     */
    public static String inferFieldName(String relationshipType) {
        StringBuilder fieldName = toUpperCamelCase(new StringBuilder(), relationshipType);
        fieldName.setCharAt(0, Character.toLowerCase(fieldName.charAt(0)));
        return fieldName.toString();
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
    private static StringBuilder toUpperCamelCase(StringBuilder sb, String name) {
        if (name != null && name.length() > 0) {
            if (!name.contains("_")) {
                sb.append(name.substring(0, 1).toUpperCase());
                sb.append(name.substring(1).toLowerCase());
            } else {
                String[] parts = name.split("_");
                for (String part : parts) {
                    String test = part.toLowerCase();
                    if ("has|is|a".contains(test)) {
                        continue;
                    }
                    toUpperCamelCase(sb, test);
                }
            }
        }
        return sb;
    }

}
