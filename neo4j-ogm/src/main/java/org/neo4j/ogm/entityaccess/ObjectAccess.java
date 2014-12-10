package org.neo4j.ogm.entityaccess;

import java.lang.reflect.Array;
import java.util.*;

public abstract class ObjectAccess implements PropertyWriter, RelationalWriter {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static Object merge(Class parameterType, Iterable<?> collection, Iterable<?> hydrated) {

        // basic "collection" types we will handle: List<T>, Set<T>, Vector<T>, T[]
        if (List.class.isAssignableFrom(parameterType)) {
            List<Object> list = new ArrayList<>();
            list.addAll((Collection)collection);
            if (hydrated != null && hydrated.iterator().hasNext()) {
                list = union(list, ((List<Object>) hydrated));
            }
            return list;
        }

        else if (Set.class.isAssignableFrom(parameterType)) {
            Set<Object> set = new HashSet<>();
            if (hydrated != null && hydrated.iterator().hasNext()) {
                set.addAll((Collection) hydrated);
            }
            set.addAll((Collection) collection);
            return set;
        }

        else if (Vector.class.isAssignableFrom(parameterType)) {
            Vector<Object> v = new Vector<>();
            v.addAll((Collection) collection);
            if (hydrated != null && hydrated.iterator().hasNext()) {
                v = union(v, (Vector<Object>) hydrated);
            }
            return v;
        }

        else if (parameterType.isArray()) {
            Class type = parameterType.getComponentType();
            List<Object> objects = new ArrayList<>();

            objects.addAll((Collection) collection);

            if (hydrated != null && hydrated.iterator().hasNext()) {
                objects = union(objects, Arrays.<Object>asList(hydrated));
            }

            Object array = Array.newInstance(type, ((Collection) objects).size());
            for (int i = 0; i < objects.size(); i++) {
                Array.set(array, i, objects.get(i));
            }
            return array;
        }

        else {
            throw new RuntimeException("Unsupported: " + parameterType.getName());
        }
    }

    private static ArrayList<Object> union(List<Object> list1, List<Object> list2) {
        Set<Object> set = new HashSet<>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<>(set);
    }

    private static Vector<Object> union(Vector<Object> list1, Vector<Object> list2) {
        Set<Object> set = new HashSet<>();
        set.addAll(list1);
        set.addAll(list2);
        return new Vector<>(set);
    }

}
