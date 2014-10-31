package org.neo4j.ogm.entityaccess;

import java.lang.reflect.Array;
import java.util.*;

public abstract class ObjectAccess {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static Object merge(Class parameterType, Iterable<?> collection, Iterable<?> hydrated) throws Exception {

        // basic "collection" types we will handle: List<T>, Set<T>, Vector<T>, T[]
        if (List.class.isAssignableFrom(parameterType)) {
            System.out.println("assignable from list");
            List<Object> list = new ArrayList<>();
            list.addAll((Collection)collection);
            if (hydrated != null && hydrated.iterator().hasNext()) {
                list = union(list, ((List<Object>) hydrated));
            }
            return list;
        }

        else if (Set.class.isAssignableFrom(parameterType)) {
            System.out.println("assignable from set");
            Set<Object> set = new HashSet<>();
            if (hydrated != null && hydrated.iterator().hasNext()) {
                set.addAll((Collection) hydrated);
            }
            set.addAll((Collection) collection);
            return set;
        }

        else if (Vector.class.isAssignableFrom(parameterType)) {
            System.out.println("assignable from vector");
            Vector<Object> v = new Vector<>();
            v.addAll((Collection) collection);
            if (hydrated != null && hydrated.iterator().hasNext()) {
                v = union(v, (Vector<Object>) hydrated);
            }
            return v;
        }

        else if (parameterType.isArray()) {
            System.out.println("assignable from array");
            Class type = parameterType.getComponentType();
            System.out.println(type.getName());
            List<Object> objects = new ArrayList<>();

            //if (collection != null) {
                objects.addAll((Collection) collection);
            //}

            if (hydrated != null && hydrated.iterator().hasNext()) {
                objects = union(objects, Arrays.<Object>asList(hydrated));
            }

            Object array = Array.newInstance(type, ((Collection) objects).size());

            System.out.println(array.getClass());
            System.out.println(objects.size());

            for (int i = 0; i < objects.size(); i++) {
                Object object = objects.get(i);
                System.out.println(object.getClass());
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
