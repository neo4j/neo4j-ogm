package org.neo4j.ogm.entityaccess;

/**
 * Implementation of {@link EntityAccessFactory} that provides {@link FieldEntityAccess} instances.
 */
public class FieldEntityAccessFactory implements EntityAccessFactory {

    @Override
    public FieldEntityAccess forProperty(String property) {
        return FieldEntityAccess.forProperty(property);
    }

}
