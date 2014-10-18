package org.neo4j.ogm.strategy.simple;

import java.util.Set;

import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;

/**
 * Implementation of {@link AttributeDictionary} that follows simple naming and structural conventions.
 */
public class SimpleAttributeDictionary implements AttributeDictionary {

    @Override
    public Set<String> lookUpCompositeEntityAttributesFromType(Class<?> typeToPersist) {
        /*
         * so, here, we will have to look at the structure of the type to persist
         * if we are doing a method-driven approach then we'll look at its setter/getter methods
         * if we are doing a field-driven approach then we'll be looking at its fields
         *
         * this seems to suggest it should be on a method/field dictionary after all
         *
         * MAYBE a field/method dictionary should implement AttributeDictionary!?
         * yes, that totally works, since the simple, method way is to do this:
         *
         *   return method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
         *
         * ...which can be backed by the method cache, if necessary.
         * So, ObjectGraphMapper can code against AttributeDictionary like it does at the moment
         * and the existing dictionaries can be made bidirectional through it.  Lovely stuff!
         *
         * That's next week's work sorted, then!
         */

        throw new UnsupportedOperationException(
                "Haven't get decided how to discern whether an instance variable should get mapped to a property or not!");
    }

    @Override
    public Set<String> lookUpValueAttributesFromType(Class<?> typeToPersist) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

    @Override
    public String lookUpRelationshipTypeForAtrribute(String attributeName) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

    @Override
    public String lookUpPropertyNameForAttribute(String attributeName) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

}
