package org.neo4j.spring.reflection;

public interface Ref<T> {
    void set(T t);
    T get();
}
