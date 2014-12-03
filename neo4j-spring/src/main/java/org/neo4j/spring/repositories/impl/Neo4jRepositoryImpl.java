package org.neo4j.spring.repositories.impl;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.spring.repositories.Neo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

@Repository
public class Neo4jRepositoryImpl<T> implements Neo4jRepository<T> {

    @Autowired
    private Session session;

    @Override
    public T load(Class<T> type, Long id) {
        return session.load(type, id);
    }

    @Override
    public T load(Class<T> type, Long id, int depth) {
        return session.load(type, id, depth);
    }

    @Override
    public Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return session.loadAll(type, ids);
    }

    @Override
    public Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth) {
        return session.loadAll(type, ids, depth);
    }

    @Override
    public Collection<T> loadAll(Class<T> type) {
        return session.loadAll(type);
    }

    @Override
    public Collection<T> loadAll(Class<T> type, int depth) {
        return session.loadAll(type, depth);
    }

    @Override
    public Collection<T> loadAll(Collection<T> objects) {
        return session.loadAll(objects);
    }

    @Override
    public Collection<T> loadAll(Collection<T> objects, int depth) {
        return session.loadAll(objects, depth);
    }

    @Override
    public void execute(String jsonStatements) {
        session.execute(jsonStatements);
    }

    @Override
    public void purge() {
        session.purge();
    }

    @Override
    public void save(T object) {
        session.save(object);
    }

    @Override
    public void save(T object, int depth) {
        session.save(object, depth);
    }

    @Override
    public void delete(T object) {
        session.delete(object);
    }

    @Override
    public void deleteAll(Class<T> type) {
        session.deleteAll(type);
    }

    @Override
    public Transaction beginTransaction() {
        return session.beginTransaction();
    }

    @Override
    public void close() {
        session.close();
    }

    @Override
    public void flush() {
        session.flush();
    }

    private Type runtimeTypeOfT() {

        final Type superclass = this.getClass().getGenericSuperclass();

        System.out.println("class: " + this.getClass());
        System.out.println("superclass type params: " + superclass.getClass().getTypeParameters().length);

        // test if an anonymous class was employed during the call
        if ( !(superclass instanceof Class) ) {
            throw new RuntimeException("This instance should belong to an anonymous class");
        }

//        if (superclass instanceof Class) {
//            TypeVariable typeVariable = ((Class) superclass).getTypeParameters()[0];
//            return typeVariable;
//            //return ((Class) superclass).getTypeParameters()[0];
//        }

        final Type[] types = ((ParameterizedType) superclass).getActualTypeArguments();
        if (types.length > 0) {
            return types[0];
        } else {
            return Object.class;
        }
    }


}
