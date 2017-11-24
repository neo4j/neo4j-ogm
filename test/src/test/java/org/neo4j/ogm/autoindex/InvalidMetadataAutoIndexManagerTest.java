package org.neo4j.ogm.autoindex;

import org.junit.Test;
import org.neo4j.ogm.domain.autoindex.NoPropertyCompositeIndexEntity;
import org.neo4j.ogm.domain.autoindex.invalid.WrongPropertyCompositeIndexEntity;
import org.neo4j.ogm.exception.core.MetadataException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Frantisek Hartman
 */
public class InvalidMetadataAutoIndexManagerTest {

    @Test(expected = MetadataException.class)
    public void shouldCheckPropertiesMatchFieldNames() throws Exception {
        MetaData metadata = new MetaData(WrongPropertyCompositeIndexEntity.class.getName());
        ClassInfo classInfo = metadata.classInfo(WrongPropertyCompositeIndexEntity.class.getName());
        classInfo.getCompositeIndexes();
    }

    @Test(expected = MetadataException.class)
    public void shouldCheckPropertiesExistsForCompositeIndex() throws Exception {
        MetaData metadata = new MetaData(NoPropertyCompositeIndexEntity.class.getName());
        ClassInfo classInfo = metadata.classInfo(NoPropertyCompositeIndexEntity.class.getName());
        classInfo.getCompositeIndexes();
    }
}
