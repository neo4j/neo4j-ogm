package org.neo4j.spring.reflection;

import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.Type;

import static org.junit.Assert.assertEquals;


public class TestTypeReflector {

    @Test
    public void notPossibleFromNonExtendedClass() {
        TypeReflector typeReflector = new TypeReflector();
        Ref<String> stringTypeRef = new TypeRef<>();
        Type t = typeReflector.getGenericParameterType(stringTypeRef, 0);
        assertEquals(TypeVariableImpl.class, t.getClass());
    }


    @Test
    public void possibleFromExtendedClass() {
        TypeReflector typeReflector = new TypeReflector();
        Ref<String> stringTypeRef = new TypeRef<String>() {};
        Type t = typeReflector.getGenericParameterType(stringTypeRef, 0);
        assertEquals(String.class, t);
    }

    @Test
    public void notPossibleFromGenericFactoryMethod() {
        TypeReflector typeReflector = new TypeReflector();
        TypeRef<String> stringTypeRef = genericRef(String.class);
        Type t = typeReflector.getGenericParameterType(stringTypeRef, 0);
        assertEquals(TypeVariableImpl.class, t.getClass());
    }

    @Test
    public void possibleFromTypedFactoryMethod() {
        TypeReflector typeReflector = new TypeReflector();
        TypeRef<String> stringTypeRef = stringRef(String.class);
        Type t = typeReflector.getGenericParameterType(stringTypeRef, 0);
        assertEquals(String.class, t);
    }


    @Test
    public void possibleFromAnonymousInterfaceImpl() {
        TypeReflector typeReflector = new TypeReflector();
        Ref<String> stringTypeRef = new Ref<String>() {
            @Override
            public void set(String s) {
            }
            @Override
            public String get() {
                return null;
            }
        };
        Type t = typeReflector.getGenericParameterType(stringTypeRef, 0);
        assertEquals(String.class, t);    }

    // the diamond type <T> produces full type erasure at
    // runtime, but not at compile time, where T is known.
    private <T> TypeRef<T> genericRef(Class<? extends T> T) {
        return new TypeRef<T>() {};
    }

    // creating a parameterised class with a concrete type
    // will preserve type information only if the class
    // is wrapped in an anonymous block.
    private TypeRef<String> stringRef(Class<? extends String> S) {
        return new TypeRef<String>() {};
    }
}
