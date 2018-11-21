package org.neo4j.ogm.domain.hierarchy.domain.custom_id;

import java.util.UUID;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

public abstract class MostBasicEntity {

    @Id
    @Convert(UuidStringConverter.class)
    private UUID myId;

    public UUID getMyId() {
        return myId;
    }

    public void setMyId(UUID myId) {
        this.myId = myId;
    }
}
