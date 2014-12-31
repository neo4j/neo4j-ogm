package org.neo4j.ogm.unit.typeconversion;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.enums.Algebra;
import org.neo4j.ogm.domain.convertible.enums.Gender;
import org.neo4j.ogm.domain.convertible.enums.NumberSystem;
import org.neo4j.ogm.domain.convertible.enums.Person;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestEnumConversion {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.enums");
    private static final ClassInfo algebraInfo = metaData.classInfo("Algebra");
    private static final ClassInfo personInfo = metaData.classInfo("Person");

    @Test
    public void testSave() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertTrue(fieldInfo.isConvertible());

        Algebra algebra = new Algebra();
        algebra.setNumberSystem(NumberSystem.NATURAL);
        assertEquals("N", algebra.getNumberSystem().getDomain());
        String value = fieldInfo.converter().toGraphProperty(algebra.getNumberSystem());
        // the converted enum value that will be stored as a neo4j node / rel property
        assertEquals("NATURAL", value);
    }

    @Test
    public void testLoad() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertTrue(fieldInfo.isConvertible());
        // a node / rel property value loaded from neo4j, to be stored in on an enum
        String value = "INTEGER";
        Algebra algebra = new Algebra();
        algebra.setNumberSystem((NumberSystem) fieldInfo.converter().toEntityAttribute(value));

        assertEquals(NumberSystem.INTEGER, algebra.getNumberSystem());
        assertEquals("Z", algebra.getNumberSystem().getDomain());
    }

    @Test
    public void testGenderMale() {

        Person bob = new Person();
        bob.setGender(Gender.MALE);

        FieldInfo fieldInfo = personInfo.propertyField("gender");
        assertTrue(fieldInfo.isConvertible());

        assertEquals("MALE", fieldInfo.converter().toGraphProperty(bob.getGender()));

    }

}
