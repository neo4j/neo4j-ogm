package org.neo4j.ogm.metadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotations.DefaultEntityAccessStrategy;
import org.neo4j.ogm.annotations.PropertyReader;
import org.neo4j.ogm.domain.food.entities.Chicken;
import org.neo4j.ogm.domain.food.entities.Pizza;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author Mihai Raulea
 * @see @145
 */
@Ignore
public class OutOfScopeTest extends MultiDriverTestClass {

    Session session;
    MetaData metaData;
    Pizza pizza = new Pizza();
    Chicken chicken = new Chicken();
    DefaultEntityAccessStrategy entityAccessStrategy = new DefaultEntityAccessStrategy();
    ClassInfo classInfoPizza;
    ClassInfo classInfoChicken;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.food.entities");
        session = sessionFactory.openSession();
        metaData = new MetaData("org.neo4j.ogm.domain.food.entities");

        pizza.noOfCaloriesPer100Grams = 332;
        chicken.noOfCaloriesPer100Grams = 112;
        classInfoPizza = metaData.classInfo(pizza);
        classInfoChicken = metaData.classInfo(chicken);
    }

    // should get back to this when isSimple returns true for enum
    @Ignore
    @Test
    public void testEnumFieldHasDefaultConverterIfNoConverterPresent() {
        for (PropertyReader propertyReader : entityAccessStrategy.getPropertyReaders(classInfoChicken)) {
            Object value = propertyReader.read(chicken);
            System.out.println(propertyReader.propertyName()+" "+value);
        }

        //FieldInfo fieldInfoPizza = classInfoPizza.propertyField("nutrient");
        //Assert.assertTrue(fieldInfoPizza.hasConverter());
    }

    @Test
    public void isEnumSimpleField() {
        Collection<FieldInfo> chickenFieldInfos = classInfoChicken.propertyFields();
        Collection<FieldInfo> pizzaFieldInfos = classInfoPizza.propertyFields();
        Assert.assertTrue(chickenFieldInfos.size() == 1);
        Assert.assertTrue(pizzaFieldInfos.size() == 2);

        Collection<PropertyReader> chickenPropertyReaders = this.entityAccessStrategy.getPropertyReaders(classInfoChicken);
        Collection<PropertyReader> pizzaPropertyReaders = this.entityAccessStrategy.getPropertyReaders(classInfoPizza);
    }

    @Test
    public void testNoAnnotation() {
        session.save(chicken);
    }

    @Test
    public void testWithAnnotation() {
        Collection<PropertyReader> propertyReaders = entityAccessStrategy.getPropertyReaders(classInfoPizza);
        for (PropertyReader propertyReader : propertyReaders) {
            Object value = propertyReader.read(pizza);
            System.out.println(propertyReader.propertyName()+" : "+value);
        }
        session.save(pizza);
    }


}
