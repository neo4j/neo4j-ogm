package org.neo4j.ogm.utils;

/**
 * A basic Pair Tuple implementation.
 *
 * @author Mark Angrish
 */
public class Pair<A, B> {

    /**
     * Creates a new pair.
     */
    public static <T1, T2> Pair<T1, T2> of(T1 t1, T2 t2) {
        return new Pair<>(t1, t2);
    }

    /**
     * The first element of this <code>Pair</code>
     */
    public final A first;

    /**
     * The second element of this <code>Pair</code>
     */
    public final B second;

    /**
     * Constructs a new <code>Pair</code> with the given values.
     *
     * @param first the first element
     * @param second the second element
     */
    private Pair(A first, B second) {

        this.first = first;
        this.second = second;
    }
}
