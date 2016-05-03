/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.annotations;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.neo4j.ogm.session.Utils;


/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public abstract class EntityAccess implements PropertyWriter, RelationalWriter {


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object merge(Class<?> parameterType, Object newValues, Object[] currentValues) {
        if (currentValues != null) {
            return merge(parameterType, newValues, Arrays.asList(currentValues));
        } else {
            return merge(parameterType, newValues, new ArrayList());
        }
    }


    /**
     * Merges the contents of <em>collection</em> with <em>hydrated</em> ensuring no duplicates and returns the result as an
     * instance of the given parameter type.
     *
     * @param parameterType The type of Iterable or array to return
     * @param newValues The objects to merge into a collection of the given parameter type, which may not necessarily be of a
     *        type assignable from <em>parameterType</em> already
     * @param currentValues The Iterable to merge into, which may be <code>null</code> if a new collection needs creating
     * @return The result of the merge, as an instance of the specified parameter type
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object merge(Class<?> parameterType, Object newValues, Collection currentValues) {

        //While we expect newValues to be an iterable, there are a couple of exceptions

        //1. A primitive array cannot be cast directly to Iterable
        newValues = boxPrimitiveArray(newValues);

        //2. A char[] may come in as a String or an array of String[]
        newValues = stringToCharacterIterable(newValues, parameterType);


        if (parameterType.isArray()) {
            Class type = parameterType.getComponentType();
            List<Object> objects = new ArrayList<>(union((Collection) newValues, currentValues));

            Object array = Array.newInstance(type, objects.size());
            for (int i = 0; i < objects.size(); i++) {
                Array.set(array, i, Utils.coerceTypes(type, objects.get(i)));
            }
            return array;
        }

        // create the desired type of collection and use it for the merge
        Collection newCollection = createCollection(parameterType, (Collection) newValues, currentValues);
        if (newCollection != null) {
            return newCollection;
        }

        // hydrated is unusable at this point so we can just set the other collection if it's compatible
        if (parameterType.isAssignableFrom(newValues.getClass())) {
            return newValues;
        }


        throw new RuntimeException("Unsupported: " + parameterType.getName());
    }

    private static Collection<?> createCollection(Class<?> parameterType, Collection collection, Collection hydrated) {
        if (Vector.class.isAssignableFrom(parameterType)) {
            return new Vector<>(union(collection, hydrated));
        }
        if (List.class.isAssignableFrom(parameterType)) {
            return new ArrayList<>(union(collection, hydrated));
        }
        if (SortedSet.class.isAssignableFrom(parameterType)) {
            return new TreeSet<>(union(collection, hydrated));
        }
        if (Set.class.isAssignableFrom(parameterType)) {
            return new HashSet<>(union(collection, hydrated));
        }
        return null;
    }

    private static Collection<Object> union(Collection collection, Collection hydrated) {
        int resultSize = collection.size();
        if (hydrated != null) {
            resultSize += hydrated.size();
        }
        Collection<Object> result = new ArrayList<>(resultSize);

        if (hydrated != null && hydrated.size() > collection.size()) {
            result.addAll(hydrated);
            addToCollection(collection, result);
        }
        else {
            result.addAll(collection);
            if (hydrated!=null) {
                addToCollection(hydrated, result);
            }
        }
        return result;
    }

    private static void addToCollection(Collection add, Collection<Object> addTo) {
        for (Object object : add) {
			if (!addTo.contains(object)) {
				addTo.add(object);
			}
		}
    }


    /**
     * Convert to an Iterable of Character if the value is a String
     * @param value the object
     * @return List of Character if the value is a String, or the value unchanged
     */
    private static Object stringToCharacterIterable(Object value, Class parameterType) {
        if (value instanceof String) {
            char[] chars = ((String)value).toCharArray();
            List<Character> characters = new ArrayList<>(chars.length);
            for (char c : chars) {
                characters.add(Character.valueOf(c));
            }
            return characters;
        }
        if (value.getClass().isArray() && parameterType.getComponentType().equals(Character.class) && value.getClass().getComponentType().equals(String.class)) {
            String[] strings = (String[]) value;
            List<Character> characters = new ArrayList<>(strings.length);
            for (String s : strings) {
                characters.add(s.toCharArray()[0]);
            }
            return characters;
        }

        if (value.getClass().isArray() && parameterType.getComponentType().equals(String.class)) {
            String[] strings = (String[]) value;
            return Arrays.asList(strings);
        }
        return value;
    }

    private static Object boxPrimitiveArray(Object value) {
        if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive()) {
            switch (value.getClass().getComponentType().toString()) {
                case "int":
                    int[] intArray = (int[]) value;
                    List<Integer> boxedIntList = new ArrayList<>(intArray.length);
                    for (int i : intArray) {
                        boxedIntList.add(i);
                    }
                    return boxedIntList;

                case "float":
                    float[] floatArray = (float[]) value;
                    List<Float> boxedFloatList = new ArrayList<>(floatArray.length);
                    for (float f : floatArray) {
                        boxedFloatList.add(f);
                    }
                    return boxedFloatList;

                case "long":
                    long[] longArray = (long[]) value;
                    List<Long> boxedLongList = new ArrayList<>(longArray.length);
                    for (long l : longArray) {
                        boxedLongList.add(l);
                    }
                    return boxedLongList;

                case "double":
                    double[] dblArray = (double[]) value;
                    List<Double> boxedDoubleList = new ArrayList<>(dblArray.length);
                    for (double d : dblArray) {
                        boxedDoubleList.add(d);
                    }
                    return boxedDoubleList;

                case "boolean":
                    boolean[] booleanArray = (boolean[]) value;
                    List<Boolean> boxedBooleanList = new ArrayList<>(booleanArray.length);
                    for (boolean b : booleanArray) {
                        boxedBooleanList.add(b);
                    }
                    return boxedBooleanList;

                case "char":
                    char[] charArray = (char[]) value;
                    List<Character> boxedCharList = new ArrayList<>(charArray.length);
                    for (char c : charArray) {
                        boxedCharList.add(c);
                    }
                    return boxedCharList;
            }
        }
        return value;
    }

}
