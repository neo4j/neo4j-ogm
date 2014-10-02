package org.neo4j.ogm.entityaccess;

public abstract class AbstractEntityAccess implements EntityAccess {

    @Override
    public void set(Object instance, Object any) throws Exception {
        if (Iterable.class.isAssignableFrom(any.getClass())) {
            setIterable(instance, (Iterable<?>) any);
        } else {
            setValue(instance, any);
        }
    }

}
