package org.neo4j.ogm.entityaccess;

import org.neo4j.graphmodel.NodeModel;
import org.neo4j.graphmodel.RelationshipModel;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.*;

public class ObjectFactory {

    private final Map<String, String> taxaLeafClass = new HashMap<>();

    private final MetaData metadata;

    public ObjectFactory(MetaData metadata) {
        this.metadata = metadata;
    }

    public <T> T newObject(NodeModel nodeModel) {
        return instantiateObjectFromTaxa(nodeModel.getLabels());
    }

    public <T> T newObject(RelationshipModel edgeModel) {
        return instantiateObjectFromTaxa(edgeModel.getType());
    }

    private <T> T instantiateObjectFromTaxa(String... taxa) {

        if (taxa.length == 0) {
            throw new MappingException("Cannot map to a class with no taxa by which to determine the class name.");
        }

        String fqn = resolve(taxa);

        try {
            @SuppressWarnings("unchecked")
            Class<T> className = (Class<T>) Class.forName(fqn);
            return className.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new MappingException("Unable to instantiate class: " + fqn, e);
        }
    }

    private String resolve(String... taxa) {

        String fqn = taxaLeafClass.get(Arrays.toString(taxa));

        if (fqn == null) {
            ClassInfo classInfo = metadata.resolve(taxa);
            if (classInfo != null) {
                taxaLeafClass.put(Arrays.toString(taxa), fqn=classInfo.name());
            } else {
                throw new MappingException("Could not resolve a single base class from " + taxa);
            }
        }
        return fqn;
    }

}