package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.strategy.simple.MethodCache;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

public class MethodEntityAccess extends AbstractEntityAccess {

    private static final MethodCache methodCache = new MethodCache();

    private String setterName;
    private String getterName;

    private MethodEntityAccess(String methodName) {
        setAccessors(methodName);
    }

    private void setAccessors(String methodName) {
        this.setterName = methodName;
        this.getterName = methodName.replace("set", "get");
    }


    public static MethodEntityAccess forProperty(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("set");
        if (name != null && name.length() > 0) {
            sb.append(name.substring(0,1).toUpperCase());
            sb.append(name.substring(1));
            return new MethodEntityAccess(sb.toString());
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object instance, Object parameter) throws Exception {
        Method method=findSetter(instance, parameter, setterName);
        method.invoke(instance, parameter);
    }

    @Override
    public void setIterable(Object instance, Iterable<?> iterable) throws Exception {

        Object typeInstance = iterable;

        if (Collection.class.isAssignableFrom(iterable.getClass())) {
            // TODO: what should we do if this is an empty collection?
            typeInstance=iterable.iterator().next();
        }
        Method setter = findParameterisedSetter(instance, typeInstance, setterName);
        if (!setter.getName().equals(setterName)) {
            setAccessors(setter.getName());
            methodCache.insert(instance.getClass(), setterName + "?", setter);
        }
        Method getter = findGetter(instance, typeInstance, getterName);
        setter.invoke(instance, cast(setter, iterable, (Iterable<?>) getter.invoke(instance)));
    }

    private static Method findSetter(Object instance, Object parameter, String methodName) throws NoSuchMethodException {
        //System.out.println("Looking for method " + setterName + "(" + parameter.getClass().getSimpleName() + ") in class " + instance.getClass().getName());
        Class<?> clazz = instance.getClass();
        Method m = methodCache.lookup(clazz, methodName);
        if (m == null) {
            for (Method method : clazz.getMethods()) {
                if( Modifier.isPublic(method.getModifiers()) &&
                        method.getReturnType().equals(void.class) &&
                        method.getName().startsWith(methodName) &&
                        method.getParameterTypes().length == 1 &&
                        (method.getParameterTypes()[0] == parameter.getClass() || method.getParameterTypes()[0].isAssignableFrom(primitive(parameter.getClass())))) {
                    return methodCache.insert(clazz, methodName, method);
                }
            }
        } else {
            return m;
        }
        throw new NoSuchMethodException("Cannot find method " + methodName + "(" + parameter.getClass().getSimpleName() + ") in class " + instance.getClass().getName());
    }

    /*
     *  WIP. This is far too vague.
     */
    private static Method findGetter(Object instance, Object parameter, String methodName) throws NoSuchMethodException {
        //System.out.println("Looking for method " + methodName + " returning type " + parameter.getClass().getSimpleName() + " in class " + instance.getClass().getName());
        Class<?> clazz = instance.getClass();
        Method m = methodCache.lookup(clazz, methodName);
        if (m == null) {
            for (Method method : clazz.getMethods()) {
                if( Modifier.isPublic(method.getModifiers()) &&
//                        method.getReturnType().equals(parameter.getClass())) {//&&
                        method.getName().equals(methodName)) {//&&
//                        method.getParameterTypes().length == 1 &&
//                        (method.getParameterTypes()[0] == parameter.getClass() || method.getParameterTypes()[0].isAssignableFrom(primitive(parameter.getClass())))) {
                    return methodCache.insert(clazz, methodName, method);
                }
            }
        } else {
            return m;
        }
        throw new NoSuchMethodException("Could not find method " + methodName + " returning type " + parameter.getClass().getSimpleName() + " in class " + instance.getClass().getName());
    }

    private static Class primitive(Class clazz) {
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

    private static Method findParameterisedSetter(Object instance, Object type, String methodName) throws NoSuchMethodException {
        //System.out.println("Looking for method " + setterName + "?(Iterable<T>) in class " + instance.getClass().getName());
        Class<?> clazz = instance.getClass();
        Method method = methodCache.lookup(clazz, methodName + "?"); // ? indicates a non-scalar
        if (method == null) {
            for (Method m : instance.getClass().getMethods()) {
                if (Modifier.isPublic(m.getModifiers()) &&
                        m.getReturnType().equals(void.class) &&
                        m.getName().startsWith(methodName) &&
                        m.getParameterTypes().length == 1 &&
                        m.getGenericParameterTypes().length == 1) {
                    Type t = m.getGenericParameterTypes()[0];
                    if (t.toString().contains(type.getClass().getName())) {
                        return methodCache.insert(clazz, methodName + "?", m);
                    }
                }
            }
        }  else {
            return method;
        }
        throw new NoSuchMethodException("Cannot find method " + methodName + "?(Iterable<T>) in class " + instance.getClass().getName());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object cast(Method method, Iterable<?> collection, Iterable<?> hydrated) throws Exception {

        // basic "collection" types we will handle: List<T>, Set<T>, Vector<T>, T[]
        Class parameterType = method.getParameterTypes()[0];

        if (parameterType == List.class) {
            List<Object> list = new ArrayList<>();
            if (hydrated != null && hydrated.iterator().hasNext()) {
                list.addAll((Collection) hydrated);
            }
            list.addAll((Collection)collection);
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
            if (hydrated != null && hydrated.iterator().hasNext()) {
                v.addAll((Collection) hydrated);
            }
            v.addAll((Collection) collection);
            return v;
        }

        else if (parameterType.isArray()) {
            Class type = parameterType.getComponentType();
            Object array = Array.newInstance(type, ((Collection) collection).size());
            List<Object> objects = new ArrayList<>();
            if (hydrated != null && hydrated.iterator().hasNext()) {
                objects.addAll((Collection) hydrated);
            }
            objects.addAll((Collection) collection);
            for (int i = 0; i < objects.size(); i++) {
                Array.set(array, i, objects.get(i));
            }
            return array;
        }

        else {
            throw new RuntimeException("Unsupported: " + parameterType.getName());
        }
    }
}
