package org.neo4j.ogm.entityaccess;

/**
 * Implementation of {@link EntityAccessFactory} that provides {@link FieldEntityAccess} instances.
 */
public class FieldEntityAccessFactory implements EntityAccessFactory {

    @Override
    public FieldEntityAccess forProperty(String property) {
        return FieldEntityAccess.forProperty(property);
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
