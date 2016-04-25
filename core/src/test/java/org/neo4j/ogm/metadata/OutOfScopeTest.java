package org.neo4j.ogm.metadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.food.entities.*;
import org.neo4j.ogm.domain.food.entities.DiabetesRisk;
import org.neo4j.ogm.domain.food.outOfScope.Nutrient;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;

/**
 * @author Mihai Raulea
 * @see @145
 */
@Ignore
public class OutOfScopeTest extends MultiDriverTestClass {

    Session session;
    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.food.entities");
        session = sessionFactory.openSession();
    }

    @Test
    public void testOutscopeEnumNoPropertyNoConverter() throws InstantiationException, IllegalAccessException,IOException {
        PizzaOutscope pizza1 = createObjectOutscope(PizzaOutscopeEnumNoPropertyAnnotationNoConverter.class);
        storeAndRetrieveOutscope(pizza1);
    }

    @Test
    public void testOutscopeEnumNoPropertyWithConverter() throws InstantiationException, IllegalAccessException {
        PizzaOutscope pizza2 = createObjectOutscope(PizzaOutscopeEnumWithPropertyAnnotationWithConverter.class);
        storeAndRetrieveOutscope(pizza2);
    }

    @Test
    public void testOutscopeEnumWithPropertyNoConverter() throws InstantiationException, IllegalAccessException {
        PizzaOutscope pizza3 = createObjectOutscope(PizzaOutscopeEnumWithPropertyAnnotationNoConverter.class);
        storeAndRetrieveOutscope(pizza3);
    }

    @Test
    public void testOutscopeEnumWithPropertyWithConverter() throws InstantiationException, IllegalAccessException {
        PizzaOutscope pizza4 = createObjectOutscope(PizzaOutscopeEnumWithPropertyAnnotationWithConverter.class);
        storeAndRetrieveOutscope(pizza4);
    }

    @Test
    public void testScannedEnumNoPropertyNoConverter() throws InstantiationException, IllegalAccessException {
        PizzaScannedEnum pizzaScannedEnum1 = createObjectScannedEnum(PizzaWithScannedEnumNoPropertyAnnotationNoConverter.class);
        storeAndRetrieveScanned(pizzaScannedEnum1);
    }

    @Test
    public void testScannedEnumNoPropertyWithConverter() throws InstantiationException, IllegalAccessException {
        PizzaScannedEnum pizzaScannedEnum2 = createObjectScannedEnum(PizzaWithScannedEnumNoPropertyAnnotationWithConverter.class);
        storeAndRetrieveScanned(pizzaScannedEnum2);
    }

    @Test
    public void testScannedEnumWithPropertyNoConverter() throws InstantiationException, IllegalAccessException {
        PizzaScannedEnum pizzaScannedEnum3 = createObjectScannedEnum(PizzaWithScannedEnumWithPropertyAnnotationNoConverter.class);
        storeAndRetrieveScanned(pizzaScannedEnum3);
    }

    @Test
    public void testScannedEnumWithPropertyWithConverter() throws InstantiationException, IllegalAccessException {
        PizzaScannedEnum pizzaScannedEnum4 = createObjectScannedEnum(PizzaWithScannedEnumWithPropertyAnnotationWithConverter.class);
        storeAndRetrieveScanned(pizzaScannedEnum4);
    }

    private PizzaOutscope createObjectOutscope(Class clazz) throws InstantiationException, IllegalAccessException {
        PizzaOutscope pizzaOutscope = null;
        pizzaOutscope = (PizzaOutscope)clazz.newInstance();
        pizzaOutscope.noOfCalories = Math.ceil(Math.random()*200);
        pizzaOutscope.outscopeNutrient = Nutrient.FAT;
        return pizzaOutscope;
    }

    private PizzaScannedEnum createObjectScannedEnum(Class clazz) throws InstantiationException, IllegalAccessException {
        PizzaScannedEnum pizzaScannedEnum = null;
        pizzaScannedEnum = (PizzaScannedEnum)clazz.newInstance();
        pizzaScannedEnum.noOfCalories = Math.ceil(Math.random()*200);
        pizzaScannedEnum.diabetesRisk = DiabetesRisk.HIGH;
        return pizzaScannedEnum;
    }

    private void storeAndRetrieveOutscope(PizzaOutscope pizzaOutscope) {
        session.save(pizzaOutscope);
        PizzaOutscope retrieved = session.load(pizzaOutscope.getClass(), pizzaOutscope.id);
        Assert.assertTrue(retrieved != null);
        Assert.assertTrue(retrieved.id == pizzaOutscope.id);
        Assert.assertTrue(retrieved.noOfCalories == pizzaOutscope.noOfCalories);
        Assert.assertTrue(retrieved.outscopeNutrient == pizzaOutscope.outscopeNutrient);
    }

    private void storeAndRetrieveScanned(PizzaScannedEnum pizzaScannedEnum) {
        session.save(pizzaScannedEnum);
        PizzaScannedEnum retrieved = session.load(pizzaScannedEnum.getClass(), pizzaScannedEnum.id);
        Assert.assertTrue(retrieved != null);
        Assert.assertTrue(retrieved.id == pizzaScannedEnum.id);
        Assert.assertTrue(retrieved.noOfCalories == pizzaScannedEnum.noOfCalories);
        Assert.assertTrue(retrieved.diabetesRisk == pizzaScannedEnum.diabetesRisk);
    }

}
