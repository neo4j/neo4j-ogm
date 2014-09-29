package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.impl.StringProperty;
import org.junit.Test;
import org.neo4j.ogm.domain.social.Person;
import org.neo4j.ogm.mapper.PropertyMapper;

public class StaticMappingMetadataTest {

    @Test
    public void shouldFollowNullObjectPatternForPropertiesThatDoNotExist() {
        Property<String, Object> propertyNotOnClass = new StringProperty("favouriteColour", "Orange");

        StaticMappingMetadata mappingMetadata = personMappingMetadata();
        PropertyMapper propertyMapper = mappingMetadata.getPropertyMapper(propertyNotOnClass);
        assertNotNull(propertyMapper);
        propertyMapper.writeToObject(new Person());
    }

    @Test
    public void shouldFindpropertyMapperInformationForGraphProperty() {
        Property<String, Object> arbitraryNodeProperty = new StringProperty("age", Integer.valueOf(42));

        StaticMappingMetadata mappingMetadata = personMappingMetadata();

        PropertyMapper propertyMapper = mappingMetadata.getPropertyMapper(arbitraryNodeProperty);
        assertNotNull(propertyMapper);
        assertEquals(arbitraryNodeProperty.getKey(), propertyMapper.getFieldName());
    }

    private static StaticMappingMetadata personMappingMetadata() {
        return new StaticMappingMetadata(Person.class,
                Arrays.asList(new RegularPersistentField("name"), new RegularPersistentField("age")));
    }

}
