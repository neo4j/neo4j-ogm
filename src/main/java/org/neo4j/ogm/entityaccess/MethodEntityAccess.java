package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;

import java.lang.reflect.Method;

/**
 * TODO: JavaDoc
 */
public class MethodEntityAccess extends AbstractEntityAccess {

    private final MethodDictionary methodDictionary;

    private String setterName;
    private String getterName;

    /**
     * Creates a methodEntityAccess instance for the named property in the graphModel
     * The discovery of which class method maps to the named property is deferred
     * until setValue or setIterable is called. Once discovered, the mappings from
     * the property to the relevant getter/setter are stored in the methodDictionary
     * for fast retrieval on subsequent calls to setValue or setIterable.
     *
     * @param graphProperty the graphProperty we want to map to via getter/setter methods.
     */
    private MethodEntityAccess(MethodDictionary dictionary, String graphProperty) {
        methodDictionary = dictionary;
        setAccessors(methodDictionary.resolveGraphAttribute(graphProperty));
    }

    private void setAccessors(String methodName) {
        this.setterName = methodName;
        this.getterName = methodName.replace("set", "get");
    }

    public static MethodEntityAccess forProperty(MethodDictionary dictionary, String name) {
        return new MethodEntityAccess(dictionary, name);
    }

    @Override
    public void setValue(Object instance, Object parameter) throws Exception {
        Method setter= methodDictionary.setter(setterName, parameter, instance);
        if (!setter.getName().equals(setterName)) {
            setAccessors(setter.getName());
        }
        setter.invoke(instance, parameter);
    }

    @Override
    public void setIterable(Object instance, Iterable<?> parameter) throws Exception {

        if (parameter.iterator().hasNext()) {
            Method setter = methodDictionary.setter(setterName, parameter, instance);

            if (!setter.getName().equals(setterName)) {
                setAccessors(setter.getName());
            }

            Method getter = methodDictionary.getter(getterName, parameter.getClass(), instance);
            setter.invoke(instance, merge(setter.getParameterTypes()[0], parameter, (Iterable<?>) getter.invoke(instance)));
        }
    }

    @Override
    public Object readValue(Object instance) throws MappingException {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

}
