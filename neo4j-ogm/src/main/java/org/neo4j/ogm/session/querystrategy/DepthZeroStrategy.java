package org.neo4j.ogm.session.querystrategy;

import org.graphaware.graphmodel.neo4j.Property;

import java.util.Collection;

public class DepthZeroStrategy implements QueryStrategy {
    @Override
    public String findOne(Long id) {
        return null;
    }

    @Override
    public String findAll(Collection<Long> ids) {
        return null;
    }

    @Override
    public String findAll() {
        return null ;
    }

    @Override
    public String findByLabel(String label) {
        return String.format("MATCH p=(n:%s) return p", label);
    }

    @Override
    public String delete(Long id) {
        return null;
    }

    @Override
    public String deleteAll(Collection<Long> ids) {
        return null;
    }

    @Override
    public String purge() {
        return null;
    }

    @Override
    public String deleteByLabel(String label) {
        return null;
    }

    @Override
    public String updateProperties(Long identity, Collection<Property<String, Object>> properties) {
        return null;
    }

    @Override
    public String createNode(Collection<Property<String, Object>> properties, Collection<String> labels) {
        return null;
    }
}
