package org.neo4j.ogm.metadata.builder;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by markangrish on 07/03/2017.
 */
public class DomainInfoBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainInfoBuilder.class);


    public static DomainInfo create(String... packages) {
        // https://github.com/ronmamo/reflections/issues/80
        final Collection<URL> urls = new HashSet<>();
        for (String pkg : packages) {
            urls.addAll(ClasspathHelper.forPackage(pkg, ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader()));
        }

        Reflections reflections =
                new Reflections(new ConfigurationBuilder()
                        .setUrls(urls)
                        .filterInputsBy(new FilterBuilder().includePackage(packages))
                        .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner()).useParallelExecutor());

        DomainInfo domainInfo = new DomainInfo();

        final Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);
        allClasses.addAll(reflections.getSubTypesOf(Enum.class));

        for (Class<?> cls : allClasses) {
            ClassInfo classInfo = ClassInfoBuilder.create(cls);

            String className = classInfo.name();
            String superclassName = classInfo.superclassName();

            LOGGER.debug("Processing: {} -> {}", className, superclassName);

            if (className != null) {
                if (cls.isAnnotation() || cls.isAnonymousClass() || cls.equals(Object.class)) {
                    continue;
                }

                ClassInfo thisClassInfo = domainInfo.classNameToClassInfo.computeIfAbsent(className, k -> classInfo);

                if (!thisClassInfo.hydrated()) {

                    thisClassInfo.hydrate(classInfo);

                    ClassInfo superclassInfo = domainInfo.classNameToClassInfo.get(superclassName);
                    if (superclassInfo == null) {

                        if (superclassName != null) {
                            domainInfo.classNameToClassInfo.put(superclassName, new ClassInfo(superclassName, thisClassInfo));
                        }
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
