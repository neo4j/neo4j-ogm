package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.impl.StringProperty;
import org.junit.Test;
import org.neo4j.ogm.testmodel.Person;

public class MappingMetadataTest {

    @Test
    public void shouldFollowNullObjectPatternForPropertiesThatDoNotExist() {
        Property<String, Object> propertyNotOnClass = new StringProperty("favouriteColour", "Orange");

        MappingMetadata mappingMetadata = personMappingMetadata();
        PropertyMapper propertyMapper = mappingMetadata.getPropertyMapper(propertyNotOnClass);
        assertNotNull(propertyMapper);
        propertyMapper.writeToObject(new Person());
    }

    @Test
    public void shouldFindpropertyMapperInformationForGraphProperty() {
        Property<String, Object> arbitraryNodeProperty = new StringProperty("age", Integer.valueOf(42));

        MappingMetadata mappingMetadata = personMappingMetadata();

        PropertyMapper propertyMapper = mappingMetadata.getPropertyMapper(arbitraryNodeProperty);
        assertNotNull(propertyMapper);
        assertEquals(arbitraryNodeProperty.getKey(), propertyMapper.getFieldName());
    }

    private static MappingMetadata personMappingMetadata() {
        return new MappingMetadata(Person.class,
                Arrays.asList(new RegularPersistentField("name"), new RegularPersistentField("age")));
    }

}
