package org.neo4j.ogm.domain.lazyloading;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.lazyloading.LazyInitializer;
import org.neo4j.ogm.lazyloading.SupportsLazyLoading;

/**
 * @author Andreas Berger
 */
public abstract class BaseEntity implements SupportsLazyLoading {

    protected transient LazyInitializer lazyInitializer;

    @Id
    @GeneratedValue
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + id + ']';
    }

    @Override
    public void setLazyInitializer(LazyInitializer lazyInitializer) {
        this.lazyInitializer = lazyInitializer;
    }

    @Override public LazyInitializer getLazyInitializer() {
        return lazyInitializer;
    }

    protected void read(String fieldName) {
        if (lazyInitializer == null) {
            return;
        }
        lazyInitializer.read(fieldName);
    }

    protected void write(String fieldName, Object value) {
        if (lazyInitializer == null) {
            return;
        }
        lazyInitializer.write(fieldName, value);
    }
}
