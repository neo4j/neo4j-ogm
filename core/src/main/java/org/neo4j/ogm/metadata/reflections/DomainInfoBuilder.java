package org.neo4j.ogm.metadata.reflections;

import java.util.Set;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by markangrish on 07/03/2017.
 */
public class DomainInfoBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainInfoBuilder.class);


    public static DomainInfo create(String... packages) {
        Reflections reflections = new Reflections(packages, new SubTypesScanner(false));

        DomainInfo domainInfo = new DomainInfo();

        final Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> cls : allClasses) {
            ClassInfo classInfo = ClassInfoBuilder.create(cls);

            String className = classInfo.name();
            String superclassName = classInfo.superclassName();

            LOGGER.debug("Processing: {} -> {}", className, superclassName);

            if (className != null) {
                if (cls.getAnnotation(Transient.class) != null || cls.isAnnotation() || cls.isAnonymousClass() || cls.isEnum() || cls.equals(Object.class) || cls.isInterface()) {
                    continue;
                }
                ClassInfo thisClassInfo = domainInfo.classNameToClassInfo.computeIfAbsent(className, k -> classInfo);

                if (!thisClassInfo.hydrated()) {

                    thisClassInfo.hydrate(classInfo);

                    ClassInfo superclassInfo = domainInfo.classNameToClassInfo.get(superclassName);
                    if (superclassInfo == null) {
                        domainInfo.classNameToClassInfo.put(superclassName, new ClassInfo(superclassName, thisClassInfo));
                    } else {
                        superclassInfo.addSubclass(thisClassInfo);
                    }
                }

                if (thisClassInfo.isEnum()) {
                    LOGGER.debug("Registering enum class: {}", thisClassInfo.name());
                    domainInfo.enumTypes.add(thisClassInfo.getUnderlyingClass());
                }
            }
        }

        domainInfo.finish();

        return domainInfo;
    }
}
