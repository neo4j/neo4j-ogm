package org.neo4j.ogm.metadata;

import java.util.Collection;

/**
 * Thrown if the result of a lookup operation from a class dictionary yields more than a single unexpected result.
 */
public class DuplicateClassLookupException extends MappingException {

    private static final long serialVersionUID = 2981431919011569981L;

    /**
     * Constructs a new {@link DuplicateClassLookupException} based on the given arguments.
     *
     * @param classSimpleName The simple class name that was looked up
     * @param duplicates The collection of offending duplicate classes
     */
    public DuplicateClassLookupException(String classSimpleName, Collection<? extends Object> duplicates) {
        super("More than one class in classpath found for simple name " + classSimpleName + ": " + duplicates);
    }

}
