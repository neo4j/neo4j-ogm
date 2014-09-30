package org.neo4j.ogm.strategy;

import org.neo4j.ogm.metadata.PersistentField;
import org.neo4j.ogm.strategy.simple.Setter;

/**
 * Implementation of {@link EntityAccessFactory} that provides {@link FieldEntityAccess} instances.
 */
public class FieldEntityAccessFactory implements EntityAccessFactory {

    @Override
    public FieldEntityAccess forPersistentField(PersistentField pf) {
        return new FieldEntityAccess(String.valueOf(pf.getJavaObjectFieldName()));
    }

    @Override
    public FieldEntityAccess forType(Class<?> type) {
        // This is so we can find something like setWheels from class Wheel
        throw new UnsupportedOperationException("This method should be called on PersistentFieldDictionary.\n"
                + "My understanding is that this method is for finding, say, setWheels based on class Wheel "
                + "but that sort of inference shouldn't be done by this object.");
    }

}
