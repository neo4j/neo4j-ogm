package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassDictionary;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Class dictionary. Given a simple class name, will attempt to find the fqns of all classes
 * matching that name in the current classloader's classpath.
 */
public class SimpleClassDictionary implements ClassDictionary {

    private Collection<String> packages;

    private Map<String, String> fqns = new HashMap<>();
    private Map<String, String> taxaLeafClass = new HashMap<>();

    private Map<String, Set<String>> subClasses = new HashMap();

    /**
     * Returns the fully qualified class name represented by simpleName, if it exists
     * Throws a RuntimeException if more than one qualified class name matches.
     * @param simpleName The simple name of the class whose fully qualified name we wish to find.
     * @return
     */
    private String getFQN(String simpleName) {

        if (packages == null) {
            packages = getPackages();
        }

        String qualifiedName = fqns.get(simpleName);

        if (qualifiedName == null) {
            List<String> qualifiedNames = scanPackages(simpleName);
            if (!qualifiedNames.isEmpty()) {
                if (qualifiedNames.size() > 1) {
                    throw new RuntimeException("More than one class in classpath found for simple name " + simpleName + ": " + qualifiedNames);
                } else {
                    qualifiedName = qualifiedNames.get(0);
                    this.fqns.put(simpleName, qualifiedName);
                }
            }
        }
        return qualifiedName;
    }

    /**
     * Scans all packages on the classpath to find one or more classes matching simpleName
     *
     * @param simpleName the simple name of the class to find, e.g. "FlangeBucket"
     * @return a list of fully qualified classnames that match, e.g. "com.whangdoodle.thropgirdle.FlangeBucket"
     */
    private List<String> scanPackages(String simpleName) {
        List<String> qualifiedNames = new ArrayList<>();
        for (String packageName : packages) {
            try {
                String fqn = packageName + "." + simpleName;
                Class clazz=Class.forName(fqn);
                if (Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                if (Modifier.isInterface(clazz.getModifiers())) {
                    continue;
                }
                if (clazz.getSuperclass() != null) {
                    if (!clazz.getSuperclass().getName().startsWith("java.lang")) {
                        addSubClass(clazz.getSuperclass().getName(), fqn);
                    }
                }
                qualifiedNames.add(fqn);
            } catch (Exception e) {
                // Ignore: no such class in this package
            }
        }
        return qualifiedNames;
    }

    /**
     * Registers a subClass as extending a superClass
     * @param superClass the superclass
     * @param subClass the subclass
     */
    private void addSubClass(String superClass, String subClass) {
        Set<String> classes = subClasses.get(superClass);
        if (classes == null) {
            classes = new HashSet<>();
            subClasses.put(superClass, classes);
        }
        classes.add(subClass);
    }

    /**
     * Retrieve the packages on the current classloader's class path
     * @return the classloader's packages
     */
    private static Collection<String> getPackages() {
        Set<String> packages = new HashSet<>();
        for (Package aPackage : Package.getPackages()) {
            packages.add(aPackage.getName());
        }
        return packages;
    }

    @Override
    public String determineLeafClass(String... taxa) {

        String fqn = taxaLeafClass.get(Arrays.toString(taxa));
        //System.out.println(Arrays.toString(taxa));
        if (fqn == null) {

            Set<String> fqns = new HashSet<>();

            for (String taxon : taxa) {
                fqn = getFQN(taxon);
                if (fqn != null) {
                    fqns.add(fqn);
                }
            }
            fqn = commonBaseClass(fqns, taxa);
            taxaLeafClass.put(Arrays.toString(taxa), fqn);
        }
        return fqn;
    }

    /**
     * Given an set of fully qualified names that are possibly within a type hierarchy
     * This function returns the base class from among them.
     * @param fqns A list of FQNs
     * @param taxa the taxa corresponding to the FQNs (not really needed, should do this better)
     * @return The base class of the FQNs
     */
    private String commonBaseClass(Set<String> fqns, String... taxa) {

        if (!fqns.isEmpty()) {
            Set<String> baseClasses = new HashSet<>();
            for (String fqn : fqns) {
                String baseClass = resolveBaseClass(fqn, subClasses.get(fqn));
                //System.out.println(fqn + ": resolved=" + baseClass);
                baseClasses.add(baseClass);
            }
            if (baseClasses.size() > 1) {
                System.out.println("no common baseclass for specified taxa: " + Arrays.toString(taxa) + ". roots are: " + baseClasses);
                return null;
            }
            if (baseClasses.iterator().hasNext()) {
                return baseClasses.iterator().next();
            }
        }
        return null;
    }

    private String resolveBaseClass(String fqn, Set<String> subclasses) {
        if (subclasses == null) {
            return fqn;
        }
        if (subclasses.size() > 1) {
            //System.out.println("More than one class extends " + fqn + ": " + subclasses);
            return null;
        }
        fqn = subclasses.iterator().next();
        return resolveBaseClass(fqn, subClasses.get(fqn));
    }
}
