package org.neo4j.ogm.domain.gh787;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.Convert;

/**
 * @author Gerrit Meier
 */
@NodeEntity
public class EntityWithCustomIdConverter {

    @Id
    @Convert(MyVeryOwnIdTypeConverter.class)
    private MyVeryOwnIdType key;

    public EntityWithCustomIdConverter(MyVeryOwnIdType key) {
        this.key = key;
    }

    public EntityWithCustomIdConverter() {
    }

    public MyVeryOwnIdType getKey() {
        return key;
    }
}
