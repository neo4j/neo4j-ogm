package org.neo4j.ogm.metadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.food.entities.inScope.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;

/**
 * Created by Mihai Raulea on 5/4/2016.
 * @see issue @145
 */
public class InScopeEnumTest extends MultiDriverTestClass {

    Session session;
    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.food.entities.inScope");
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void testScannedEnumNoPropertyNoConverter() throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaScannedEnum pizzaScannedEnum1 = createObjectScannedEnum(PizzaWithScannedEnumNoPropertyAnnotationNoConverter.class);
        storeAndRetrieveScanned(pizzaScannedEnum1,PizzaWithScannedEnumNoPropertyAnnotationNoConverter.class);
    }

    @Test
    public void testScannedEnumNoPropertyWithConverter() throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaScannedEnum pizzaScannedEnum2 = createObjectScannedEnum(PizzaWithScannedEnumNoPropertyAnnotationWithConverter.class);
        storeAndRetrieveScanned(pizzaScannedEnum2,PizzaWithScannedEnumNoPropertyAnnotationWithConverter.class);
    }

    @Test
    public void testScannedEnumWithPropertyNoConverter() throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaScannedEnum pizzaScannedEnum3 = createObjectScannedEnum(PizzaWithScannedEnumWithPropertyAnnotationNoConverter.class);
        storeAndRetrieveScanned(pizzaScannedEnum3,PizzaWithScannedEnumWithPropertyAnnotationNoConverter.class);
    }

    @Test
    public void testScannedEnumWithPropertyWithConverter() throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaScannedEnum pizzaScannedEnum4 = createObjectScannedEnum(PizzaWithScannedEnumWithPropertyAnnotationWithConverter.class);
        storeAndRetrieveScanned(pizzaScannedEnum4, PizzaWithScannedEnumWithPropertyAnnotationWithConverter.class);
    }

    private PizzaScannedEnum createObjectScannedEnum(Class<? extends PizzaScannedEnum> clazz) throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaScannedEnum pizzaScannedEnum = clazz.newInstance();
        pizzaScannedEnum.noOfCalories = Math.ceil(Math.random()*200);
        clazz.getField("diabetesRisk").set(pizzaScannedEnum, DiabetesRisk.HIGH);
        return pizzaScannedEnum;
    }

    private void storeAndRetrieveScanned(PizzaScannedEnum pizzaScannedEnum, Class clazz) throws  NoSuchFieldException, IllegalAccessException {
        session.save(pizzaScannedEnum);
        PizzaScannedEnum retrieved = session.load(pizzaScannedEnum.getClass(), pizzaScannedEnum.id);
        Assert.assertTrue(retrieved != null);
        Assert.assertTrue(retrieved.id == pizzaScannedEnum.id);
        Assert.assertTrue(retrieved.noOfCalories == pizzaScannedEnum.noOfCalories);
        Assert.assertTrue(clazz.getField("diabetesRisk").get(retrieved) == clazz.getField("diabetesRisk").get(pizzaScannedEnum));
    }

}
