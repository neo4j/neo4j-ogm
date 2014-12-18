package org.neo4j.ogm.entityaccess;

import java.lang.reflect.Array;
import java.util.*;

public abstract class ObjectAccess implements PropertyWriter, RelationalWriter {

    /**
     * Merges the contents of <em>collection</em> with <em>hydrated</em> ensuring no duplicates and returns the result as an
     * instance of the given parameter type.
     *
     * @param parameterType The type of Iterable or array to return
     * @param collection The objects to merge into a collection of the given parameter type, which may not necessarily be of a
     *        type assignable from <em>parameterType</em> already
     * @param hydrated The Iterable to merge into, which may be <code>null</code> if a new collection needs creating
     * @return The result of the merge, as an instance of the specified parameter type
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static Object merge(Class parameterType, Iterable<?> collection, Iterable<?> hydrated) {

        if (parameterType.isArray()) {
            Class type = parameterType.getComponentType();
            List<Object> objects = new ArrayList<>(union(collection, hydrated));

            Object array = Array.newInstance(type, objects.size());
            for (int i = 0; i < objects.size(); i++) {
                Array.set(array, i, objects.get(i));
            }
            return array;
        }

        // we don't know how to make the requested parameter type, so let's just try to work with what we've got
        if (hydrated != null && parameterType.isAssignableFrom(hydrated.getClass())) {
            if (Collection.class.isAssignableFrom(hydrated.getClass())) {
                Collection col = (Collection) hydrated;
                for (Object object : collection) {
                    if (!col.contains(object)) {
                        col.add(object);
                    }
                }
                return hydrated;
            }
        }

        // hydrated is unusable at this point so we can just set the other collection if it's compatible
        if (parameterType.isAssignableFrom(collection.getClass())) {
            return collection;
        }

        // create the desired type of collection and use it for the merge
        Collection newCollection = createCollection(parameterType, collection, hydrated);
        if (newCollection != null) {
            return newCollection;
        }

        throw new RuntimeException("Unsupported: " + parameterType.getName());
    }

    private static Collection<?> createCollection(Class<?> parameterType, Iterable<?> collection, Iterable<?> hydrated) {
        if (Vector.class.isAssignableFrom(parameterType)) {
            return new Vector<>(union(collection, hydrated));
        }
        if (List.class.isAssignableFrom(parameterType)) {
            return new ArrayList<>(union(collection, hydrated));
        }
        if (Set.class.isAssignableFrom(parameterType)) {
            return union(collection, hydrated);
        }
        return null;
    }

    private static Set<Object> union(Iterable<?> collection, Iterable<?> hydrated) {
        Set<Object> set = new HashSet<>();
        for (Object object : collection) {
            set.add(object);
        }
        if (hydrated != null) {
            for (Object object : hydrated) {
                set.add(object);
            }
        }
        return set;
    }

}
