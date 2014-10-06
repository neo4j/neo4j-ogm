package org.neo4j.ogm.metadata.dictionary;

/**
 * Provides a mechanism by which to look up a class name from a label and vice versa.
 */
public interface ClassDictionary {

    /**
     * Determines the fully-qualified class name that maps to a graph entity categorised with the given taxa.
     *
     * @param taxa The labels or types applied to the graph entity to which a class name is to be matched
     * @return The FQN of the class name that corresponds to the given taxa or <code>null</code> if no match can be found
     */
    String determineLeafClass(String... taxa);

//    /**
//     * Resolves the fully-qualified class names of all the known classes that could potentially correspond to the given simple
//     * class name.
//     *
//     * @param simpleName The simple name of the class for which to find the FQNs
//     * @return A {@link List} of fully-qualified class names that correspond to the given argument or an empty list if there
//     *         aren't any, never <code>null</code>
//     */
//    List<String> getBaseClass(String simpleName);

}
