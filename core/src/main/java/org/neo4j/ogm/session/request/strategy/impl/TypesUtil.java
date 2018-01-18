package org.neo4j.ogm.session.request.strategy.impl;

import java.util.Collection;

/**
 * @author Frantisek Hartman
 */
class TypesUtil {

    /**
     * Concatenate and escape labels
     * <p>
     * For use in MATCH queries when type is needed.
     * <p>
     * E.g. for "[Person","Entity"] labels the result will be ":`Person`:`Entity`"
     *
     * @param labels labels to
     *
     * @return concatenated labels
     */
    static String labelsToType(Iterable<String> labels) {
        StringBuilder cypherLabels = new StringBuilder();
        for (String label : labels) {
            if (label != null && !label.isEmpty()) {
                cypherLabels.append(":`").append(label).append('`');
            }
        }
        return cypherLabels.toString();
    }

    /**
     * Checks that given types collection contains exactly one element, throw {@link IllegalArgumentException} if not
     */
    static void checkSingleType(Collection<String> types) {
        if (types.size() != 1) {
            throw new IllegalArgumentException("Multiple types passed where single type is expected, types=" + types);
        }
    }
}
