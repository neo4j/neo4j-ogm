package org.neo4j.ogm.metadata;

import java.util.List;

import org.graphaware.graphmodel.Taxon;

/**
 * Provides a mechanism by which to look up a class from a label and vice versa.
 */
public interface ClassDictionary {

    /**
     * Determines the fully-qualified class name that maps to a graph entity categorised with the given taxa.
     *
     * @param taxa The taxa applied to the graph entity to which a class name is to be matched
     * @return The FQN of the class name that corresponds to the given taxa or <code>null</code> if no match can be found
     */
    String determineFqnFromTaxa(List<Taxon> taxa);

}
