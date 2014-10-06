package org.neo4j.ogm.metadata;

public abstract class ClassUtils {

    public static String primitiveArrayName(Class clazz) {

        if (clazz == Integer.class) return "[I";
        if (clazz == Long.class) return "[J";
        if (clazz == Short.class) return "[S";
        if (clazz == Byte.class) return "[B";
        if (clazz == Character.class) return "[C";
        if (clazz == Float.class) return "[F";
        if (clazz == Double.class) return "[D";
        if (clazz == Boolean.class) return "[Z";

        return "";
    }

    public static  Class unbox(Class clazz) {
        if (clazz == Void.class) {
            return void.class;
        }
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
        // single-dimension arrays
        if (clazz == Void[].class) {
            return void.class; // an array of Voids is a void.
        }
        if (clazz == Integer[].class) {
            return int[].class;
        }
        if (clazz == Long[].class) {
            return long[].class;
        }
        if (clazz == Short[].class) {
            return short[].class;
        }
        if (clazz == Byte[].class) {
            return byte[].class;
        }
        if (clazz == Float[].class) {
            return float[].class;
        }
        if (clazz == Double[].class) {
            return double[].class;
        }
        if (clazz == Character[].class) {
            return char[].class;
        }
        if (clazz == Boolean[].class) {
            return boolean[].class;
        }
        return clazz; // not a primitive, can't be unboxed.
    }


}
