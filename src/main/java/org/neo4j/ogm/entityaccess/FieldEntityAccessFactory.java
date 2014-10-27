package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.dictionary.FieldDictionary;

/**
 * Implementation of {@link EntityAccessFactory} that provides {@link FieldEntityAccess} instances.
 */
public class FieldEntityAccessFactory implements EntityAccessFactory {

    public final FieldDictionary fieldDictionary;

    public FieldEntityAccessFactory(FieldDictionary fieldDictionary) {
        this.fieldDictionary = fieldDictionary;
    }

    @Override
    public FieldEntityAccess forProperty(String property) {
        return FieldEntityAccess.forProperty(fieldDictionary, property);
    }

    @Override
    public FieldEntityAccess forAttributeOfType(String attributeName, Class<?> type) {
        return forProperty(attributeName);
    }

    @Override
    public FieldEntityAccess forIdAttributeOfType(Class<?> type) {
        // FIXME: this is a total hack, since we can't use FieldDictionary without an instance of this type
        // furthermore, we're still assuming "simple" strategy with the hard-coded dictionary in FieldEntityAccess
        return forAttributeOfType("id", type);
    }

}
