package org.neo4j.spring.reflection;

public class TypeRef<T> implements Ref<T> {

    private T t;

    public void set(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }
}


