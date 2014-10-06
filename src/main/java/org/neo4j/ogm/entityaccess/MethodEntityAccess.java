package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.MethodDictionary;
import org.neo4j.ogm.strategy.simple.SimpleMethodDictionary;

import java.lang.reflect.Method;

/**
 * TODO: JavaDoc
 */
public class MethodEntityAccess extends AbstractEntityAccess {

    // todo: don't hardwire this in. Use injection to inject what you need.
    private static final MethodDictionary methodDictionary = new SimpleMethodDictionary();

    private String setterName;
    private String getterName;

    private MethodEntityAccess(String methodName) {
        setAccessors(methodName);
    }

    private void setAccessors(String methodName) {
        this.setterName = methodName;
        this.getterName = methodName.replace("set", "get");
    }


    public static MethodEntityAccess forProperty(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("set");
        if (name != null && name.length() > 0) {
            sb.append(name.substring(0, 1).toUpperCase());
            sb.append(name.substring(1));
            return new MethodEntityAccess(sb.toString());
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object instance, Object parameter) throws Exception {
        Method setter= methodDictionary.findSetter(setterName, parameter, instance);
        if (!setter.getName().equals(setterName)) {
            setAccessors(setter.getName());
        }
        setter.invoke(instance, parameter);
    }

    @Override
    public void setIterable(Object instance, Iterable<?> parameter) throws Exception {

        if (parameter.iterator().hasNext()) {
            Method setter = methodDictionary.findSetter(setterName, parameter, instance);

            if (!setter.getName().equals(setterName)) {
                setAccessors(setter.getName());
            }

            Method getter = methodDictionary.findGetter(getterName, parameter.getClass(), instance);
            setter.invoke(instance, merge(setter.getParameterTypes()[0], parameter, (Iterable<?>) getter.invoke(instance)));
            //setter.invoke(instance, merge(setter.getParameterTypes()[0], parameter, null));

        }
    }

}
