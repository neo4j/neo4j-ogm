package org.graphaware.graphmodel.neo4j;

public class Property<K, V> {

    K key;
    V value;

    /**
     * Constructs a new {@link Property} inferring the generic type arguments of the key and the value.
     *
     * @param key The property key or name
     * @param value The property value
     * @return A new {@link Property} based on the given arguments
     */
    public static <K, V> Property<K, V> with(K key, V value) {
        return new Property<K, V>(key, value);
    }

    public Property() {}

    public Property(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
