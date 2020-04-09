package org.neo4j.ogm.domain.gh787;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Gerrit Meier
 */
public class MyVeryOwnIdType implements Serializable {
    private final String value;

    public MyVeryOwnIdType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MyVeryOwnIdType blubb = (MyVeryOwnIdType) o;
        return value.equals(blubb.value);
    }

    @Override public int hashCode() {
        return Objects.hash(value);
    }
}
