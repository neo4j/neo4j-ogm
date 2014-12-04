package org.neo4j.spring.reflection;

// this set of tests extends the TypeReflector tests
// to assert the behaviour of type reflection under spring
// autowiring conditions.


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={org.neo4j.spring.reflection.ApplicationContext.class})

public class TestSpringAutowiring {

    @Autowired
    private SpringComponent component;
    private TypeReflector typeReflector = new TypeReflector();

    @Test
    public void testAbstractWiring() {
        assertEquals(String.class, typeReflector.getGenericParameterType(component.abstractlyWiredRef, 0));
    }

    @Test
    public void testConcreteWiring() {
        assertEquals(TypeVariableImpl.class, typeReflector.getGenericParameterType(component.concretelyWiredRef, 0).getClass());
    }

    @Test
    public void testInterfaceWiring() {
        assertEquals(String.class, typeReflector.getGenericParameterType(component.interfaceWiredRef, 0));
    }

}
