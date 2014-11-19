package org.neo4j.ogm.session.strategy;

import org.neo4j.graphmodel.Property;
import org.neo4j.ogm.mapper.cypher.GraphModelQuery;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatement;
import org.neo4j.ogm.mapper.cypher.RowModelQuery;
import org.neo4j.ogm.session.Utils;

import java.util.Collection;

public class DepthZeroStrategy implements ReadStrategy, WriteStrategy {

    public GraphModelQuery findOne(Long id) {
        return null;
    }

    @Override
    public GraphModelQuery findOne(Long id, int depth) {
        return null;
    }

    public GraphModelQuery findAll(Collection<Long> ids) {
        return null;
    }

    @Override
    public GraphModelQuery findAll(Collection<Long> ids, int depth) {
        return null;
    }

    @Override
    public GraphModelQuery findAll() {
        return null ;
    }

    public GraphModelQuery findByLabel(String label) {
        return new GraphModelQuery(String.format("MATCH p=(n:%s) return p", label), Utils.map());
    }

    @Override
    public GraphModelQuery findByLabel(String label, int depth) {
        return null;
    }

    public GraphModelQuery findByProperty(String label, Property<String, Object> property) {
        return new GraphModelQuery(String.format("MATCH p=(n:%s { properties } ) return p", label), Utils.map(property.getKey(), property.asParameter()));
    }

    @Override
    public GraphModelQuery findByProperty(String label, Property<String, Object> property, int depth) {
        return null;
    }

    @Override
    public ParameterisedStatement delete(Long id) {
        return null;
    }

    @Override
    public ParameterisedStatement deleteAll(Collection<Long> ids) {
        return null;
    }

    @Override
    public ParameterisedStatement purge() {
        return null;
    }

    @Override
    public ParameterisedStatement deleteByLabel(String label) {
        return null;
    }

    @Override
    public ParameterisedStatement updateProperties(Long identity, Collection<Property<String, Object>> properties) {
        return null;
    }

    @Override
    public RowModelQuery createNode(Collection<Property<String, Object>> properties, Collection<String> labels) {
        return null;
    }
}
