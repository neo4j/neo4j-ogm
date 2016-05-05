package org.neo4j.ogm.metadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.food.entities.inScope.DiabetesRisk;
import org.neo4j.ogm.domain.food.entities.inScope.*;
import org.neo4j.ogm.domain.food.entities.outOfScope.PizzaOutscope;
import org.neo4j.ogm.domain.food.entities.outOfScope.PizzaOutscopeEnumNoPropertyAnnotationNoConverter;
import org.neo4j.ogm.domain.food.entities.outOfScope.PizzaOutscopeEnumWithPropertyAnnotationNoConverter;
import org.neo4j.ogm.domain.food.entities.outOfScope.PizzaOutscopeEnumWithPropertyAnnotationWithConverter;
import org.neo4j.ogm.domain.food.outOfScopeEnum.Nutrient;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;

/**
 * @author Mihai Raulea
 * @see issue @145
 */
@Ignore
public class OutOfScopeEnumTest extends MultiDriverTestClass {

    Session session;
    /*
    All tests fail, because an error is thrown; the enum is out of scope; @TODO the session.clear issue
     */
    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.food.entities.outOfScope");
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void testOutscopeEnumNoPropertyNoConverter() throws InstantiationException, IllegalAccessException,IOException,NoSuchFieldException {
        PizzaOutscope pizza1 = createObjectOutscope(PizzaOutscopeEnumNoPropertyAnnotationNoConverter.class);
        storeAndRetrieveOutscope(pizza1, PizzaOutscopeEnumNoPropertyAnnotationNoConverter.class);
    }

    @Test
    public void testOutscopeEnumNoPropertyWithConverter() throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaOutscope pizza2 = createObjectOutscope(PizzaOutscopeEnumWithPropertyAnnotationWithConverter.class);
        storeAndRetrieveOutscope(pizza2,PizzaOutscopeEnumWithPropertyAnnotationWithConverter.class);
    }

    @Test
    public void testOutscopeEnumWithPropertyNoConverter() throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaOutscope pizza3 = createObjectOutscope(PizzaOutscopeEnumWithPropertyAnnotationNoConverter.class);
        storeAndRetrieveOutscope(pizza3,PizzaOutscopeEnumWithPropertyAnnotationNoConverter.class);
    }

    @Test
    public void testOutscopeEnumWithPropertyWithConverter() throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaOutscope pizza4 = createObjectOutscope(PizzaOutscopeEnumWithPropertyAnnotationWithConverter.class);
        storeAndRetrieveOutscope(pizza4,PizzaOutscopeEnumWithPropertyAnnotationWithConverter.class);
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

    private PizzaOutscope createObjectOutscope(Class<? extends PizzaOutscope> clazz) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        PizzaOutscope pizzaOutscope = clazz.newInstance();
        clazz.getField("noOfCalories").set(pizzaOutscope, Math.ceil(Math.random()*200));
        clazz.getField("outscopeNutrient").set(pizzaOutscope, Nutrient.FAT);
        return pizzaOutscope;
    }

    private PizzaScannedEnum createObjectScannedEnum(Class<? extends PizzaScannedEnum> clazz) throws InstantiationException, IllegalAccessException,NoSuchFieldException {
        PizzaScannedEnum pizzaScannedEnum = clazz.newInstance();
        pizzaScannedEnum.noOfCalories = Math.ceil(Math.random()*200);
        clazz.getField("diabetesRisk").set(pizzaScannedEnum, DiabetesRisk.HIGH);
        return pizzaScannedEnum;
    }

    private void storeAndRetrieveOutscope(Object pizzaOutscope, Class<? extends PizzaOutscope> clazz) throws  NoSuchFieldException, IllegalAccessException {
        session.save(pizzaOutscope);
        // without this session.clear, the test PASSES for testOutscopeEnumNoPropertyNoConverter, but the database tells a different story :)
        session.clear();
        PizzaOutscope retrieved = session.load( clazz ,(Long)clazz.getField("id").get(pizzaOutscope));
        Assert.assertTrue(retrieved != null);
        Assert.assertTrue(retrieved.id == clazz.getField("id").get(retrieved));
        Assert.assertTrue(retrieved.noOfCalories ==(Double) clazz.getField("noOfCalories").get(pizzaOutscope));
        Assert.assertTrue(clazz.getField("outscopeNutrient").get(retrieved) == clazz.getField("outscopeNutrient").get(pizzaOutscope));
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
