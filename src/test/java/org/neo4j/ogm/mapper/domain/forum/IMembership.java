package org.neo4j.ogm.mapper.domain.forum;

import java.beans.Transient;

public interface IMembership {

    // we assert fees are stored as Integers in the DB.
    // at the moment we haven't decided what to do
    // about converting to complex number formats
    // or for that matter, dates.
    Integer getFees();

    @Transient boolean getCanPost();
    @Transient boolean getCanComment();
    @Transient boolean getCanFollow();
    @Transient IMembership[] getUpgrades();

}
