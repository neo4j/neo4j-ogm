package org.neo4j.ogm.entityaccess;

import java.lang.reflect.Array;
import java.util.*;

public abstract class AbstractEntityAccess implements EntityAccess {

    @Override
    public void set(Object instance, Object any) throws Exception {
        if (Iterable.class.isAssignableFrom(any.getClass())) {
            setIterable(instance, (Iterable<?>) any);
        } else {
            setValue(instance, any);
        }
    }

    protected static Class unbox(Class clazz) {
        if (clazz == Integer.class) {
            return int.class;
        }
        if (clazz == Long.class) {
            return long.class;
        }
        if (clazz == Short.class) {
            return short.class;
        }
        if (clazz == Byte.class) {
            return byte.class;
        }
        if (clazz == Float.class) {
            return float.class;
        }
        if (clazz == Double.class) {
            return double.class;
        }
        if (clazz == Character.class) {
            return char.class;
        }
        if (clazz == Boolean.class) {
            return boolean.class;
        }
        return clazz;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static Object merge(Class parameterType, Iterable<?> collection, Iterable<?> hydrated) throws Exception {

        // basic "collection" types we will handle: List<T>, Set<T>, Vector<T>, T[]
        if (parameterType == List.class) {

            List<Object> list = new ArrayList<>();
            list.addAll((Collection)collection);

            if (hydrated != null && hydrated.iterator().hasNext()) {
                list = union(list, ((List) hydrated));
            }

            return list;
        }

        else if (parameterType == Set.class) {
            Set<Object> set = new HashSet<>();
            if (hydrated != null && hydrated.iterator().hasNext()) {
                set.addAll((Collection) hydrated);
            }
            set.addAll((Collection) collection);
            return set;
        }

        else if (parameterType == Vector.class) {
            Vector<Object> v = new Vector<>();
            v.addAll((Collection) collection);

            if (hydrated != null && hydrated.iterator().hasNext()) {
                v = union(v, (Vector) hydrated);
            }

            return v;
        }

        else if (parameterType.isArray()) {

            Class type = parameterType.getComponentType();
            Object array = Array.newInstance(type, ((Collection) collection).size());

            List<Object> objects = new ArrayList<>();
            objects.addAll((Collection) collection);

            if (hydrated != null && hydrated.iterator().hasNext()) {
                objects = union(objects, Arrays.asList(hydrated));
            }

            for (int i = 0; i < objects.size(); i++) {
                Array.set(array, i, objects.get(i));
            }
            return array;
        }

        else {
            throw new RuntimeException("Unsupported: " + parameterType.getName());
        }
    }

    private static ArrayList union(List list1, List list2) {
        Set set = new HashSet();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList(set);
    }

    private static Vector union(Vector list1, Vector list2) {
        Set set = new HashSet();

        set.addAll(list1);
        set.addAll(list2);

        return new Vector(set);
    }

}
