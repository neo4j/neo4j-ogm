package org.neo4j.ogm.metadata;

import java.util.List;

import org.graphaware.graphmodel.Edge;
import org.graphaware.graphmodel.Taxon;
import org.graphaware.graphmodel.Vertex;

/**
 * Implementation of {@link ObjectCreator} that exclusively uses a class' default constructor for instantiation.
 */
public class DefaultConstructorObjectCreator implements ObjectCreator {

    private final ClassDictionary classDictionary;

    public DefaultConstructorObjectCreator(ClassDictionary classDictionary) {
        this.classDictionary = classDictionary;
    }

    @Override
    public <T> T instantiateObjectMappedTo(Vertex vertex) {
        return instantiateObjectFromTaxa(vertex.getTaxa());
    }

    @Override
    public <T> T instantiateObjectMappedTo(Edge edge) {
        return instantiateObjectFromTaxa(edge.getTaxa());
    }

    private <T> T instantiateObjectFromTaxa(List<Taxon> taxa) {
        if (taxa.isEmpty()) {
            throw new MappingException("Cannot map to a class with no taxa by which to determine the class name.");
        }

        String fqn = this.classDictionary.determineFqnFromTaxa(taxa);
        try {
            @SuppressWarnings("unchecked")
            Class<T> className = (Class<T>) Class.forName(fqn);
            return className.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new MappingException("Unable to instantiate class: " + fqn, e);
        }
    }

}
