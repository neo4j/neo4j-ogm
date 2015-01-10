package org.neo4j.ogm.integration.hierarchy.domain.trans;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.integration.hierarchy.domain.plain.PlainSingleClass;

/**
 *
 */
public class PlainClassWithTransientFields {

    private Long id;

    private TransientSingleClass transientField;

    @Transient
    private PlainSingleClass anotherTransientField;

    private transient PlainSingleClass yetAnotherTransientField;

    public TransientSingleClass getTransientField() {
        return transientField;
    }

    public void setTransientField(TransientSingleClass transientField) {
        this.transientField = transientField;
    }

    public PlainSingleClass getAnotherTransientField() {
        return anotherTransientField;
    }

    public void setAnotherTransientField(PlainSingleClass anotherTransientField) {
        this.anotherTransientField = anotherTransientField;
    }

    public PlainSingleClass getYetAnotherTransientField() {
        return yetAnotherTransientField;
    }

    public void setYetAnotherTransientField(PlainSingleClass yetAnotherTransientField) {
        this.yetAnotherTransientField = yetAnotherTransientField;
    }
}
