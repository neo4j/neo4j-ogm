package org.neo4j.ogm.compiler;

/**
 * Key for lookup of transient relationship by source and target
 * NOTE: source and target are always sorted so the lookup will ignore the direction
 *
 * @author Frantisek Hartman
 */
public class SrcTargetKey {

    private final long src;
    private final long tgt;

    public SrcTargetKey(long src, long tgt) {
        if (src < tgt) {
            this.src = src;
            this.tgt = tgt;
        } else {
            this.src = tgt;
            this.tgt = src;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SrcTargetKey key = (SrcTargetKey) o;

        if (src != key.src) {
            return false;
        }
        return tgt == key.tgt;
    }

    @Override
    public int hashCode() {
        int result = (int) (src ^ (src >>> 32));
        result = 31 * result + (int) (tgt ^ (tgt >>> 32));
        return result;
    }

}
