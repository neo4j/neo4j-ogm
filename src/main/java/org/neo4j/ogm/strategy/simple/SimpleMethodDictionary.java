package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * The SimpleMethodDictionary maintains mappings between labels in the graphModel, (Status, Item, Invoice, etc)
 * and the actual setter/getter methods on the class for that type.
 *
 * Where the Type relationship is scalar (i.e. where the object has a relationship to one instance
 * of the the Type only), the SimpleMethodDictionary assumes that a method name will exactly match
 * the Type name. For example:
 *
 * Type: Status must map to setStatus(...) - and no other method.
 *
 * Where the Type is a non-scalar (i.e. it exists as a collection of Type instances on the object),
 * the SimpleMethodDictionary relaxes the exact matching constraint and allows partial method matching, e.g.
 *
 * Type: Item -> to setItems(...) | setItemList(...), etc.
 *
 * In the general case, for Collection-based properties, the permitted mapping for any collection of Type T is
 *
 * Type: T -> setT*(Collection<T>)
 *
 * @author Vince Bickers
 *
 */
public class SimpleMethodDictionary extends MethodDictionary {

    protected Method findScalarSetter(Object instance, Class parameterClass, String methodName) throws MappingException {

        Class<?> clazz = instance.getClass();
        Class primitiveClass = ClassUtils.unbox(parameterClass);

        for (Method method : clazz.getMethods()) {
            if( Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().equals(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    (method.getParameterTypes()[0] == parameterClass || method.getParameterTypes()[0].isAssignableFrom(primitiveClass))) {
                return method;
            }
        }
        throw new MappingException("Cannot find method " + methodName + "(" + parameterClass.getSimpleName() + ") in class " + instance.getClass().getName());
    }

    @Override
    protected Method findGetter(String methodName, Class returnType, Object instance) throws MappingException {
        Class<?> clazz = instance.getClass();
        for (Method method : clazz.getMethods()) {
            if( Modifier.isPublic(method.getModifiers()) &&   // TODO: check return type!
                    method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new MappingException("Could not find method " + methodName + " returning type " + returnType.getClass().getSimpleName() + " in class " + instance.getClass().getName());
    }

    protected Method findCollectionSetter(Object instance, Object collection, Class elementType, String methodName) throws MappingException {
        Class<?> clazz = instance.getClass();
        for (Method method : clazz.getMethods()) {

            if (Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().startsWith(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    method.getGenericParameterTypes().length == 1) {

                if (method.getParameterTypes()[0].isArray()) {
                    String parameterName = method.getParameterTypes()[0].getName();
                    if (("[L" + elementType.getName() + ";").equals(parameterName)) {
                        return method;
                    }
                    if (ClassUtils.primitiveArrayName(elementType).equals(parameterName)) {
                        return method;
                    }
                }

                else if (method.getParameterTypes()[0].isAssignableFrom(collection.getClass())) {
                    Type t = method.getGenericParameterTypes()[0];
                    if (t.toString().contains(elementType.getName())) {
                        return method;
                    }
                    if (t.toString().contains("<?>")) {
                        return method;
                    }
                }
            }
        }

        throw new MappingException("Cannot find method " + methodName + "?(" + collection.getClass().getSimpleName() + "<" + elementType.getSimpleName() + ">) in class " + instance.getClass().getName());
    }

}
