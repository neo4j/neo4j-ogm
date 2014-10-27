package org.neo4j.ogm.metadata.dictionary;

import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;

import java.util.*;

public abstract class ClassDictionary {

    private final DomainInfo domainInfo;

    private final Map<String, String> fqns = new HashMap<>();
    private final Map<String, String> taxaLeafClass = new HashMap<>();

    public ClassDictionary(DomainInfo domainInfo) {
        this.domainInfo = domainInfo;
    }

    /**
     * Returns the fully qualified class name represented by simpleName, if it exists.
     *
     * A simple name (e.g. a label in the graph) will map to a class in the type
     * hierarchy, e.g: label "Flange" might map to "org.company.widgets.Flange"
     *
     * There must at most one such mapping possible in the type hierarchy of the domain.
     *
     * The mappings between labels and FQNs is maintained the map "fqns". If an
     * entry can't be found, it must be discovered. This is the job of the class
     * dictionary mapping implementations, Simple, Annotated, etc.
     *
     * @throws RuntimeException if more than one qualified class name matches.
     * @param simpleName The simple name of the class whose fully qualified name we wish to find.
     * @return The fully-qualified class name
     */
    private String getFQN(String simpleName) {
        String qualifiedName = fqns.get(simpleName);
        if (qualifiedName == null) {
            Class clazz = match(simpleName);
            if (clazz != null) {
                qualifiedName = clazz.getName();
                this.fqns.put(simpleName, qualifiedName);
            }
        }
        return qualifiedName;
    }

    protected DomainInfo domainInfo() {
        return domainInfo;
    }

    /**
     * Given a set of Taxa (node labels), this method determines which of
     * these represents the "leaf" class in the corresponding type hierarchy
     * of the domain being mapped.
     *
     * Taxa are presented in any order, e.g. "Account", "Gold", "User"
     *
     * In this example, assuming that "Gold" is the leaf taxon, a mapping between
     * the leaf taxon and the list of related taxa is maintained.
     *
     * Note that this method does not return the actual domain type represented
     * by the leaf taxon, though in the process of discovering what the
     * leaf taxon is, this information must in fact be uncovered.
     *
     * @param taxa
     * @return  a fqn of the leaf class corresponding to the taxa
     */
    public String determineLeafClass(String... taxa) {

        String fqn = taxaLeafClass.get(Arrays.toString(taxa));
        if (fqn == null) {
            Set<String> fqns = new HashSet<>();
            for (String taxon : taxa) {
                fqn = getFQN(taxon);  // eg getFQN("Flange") would return an FQN like "com.company.widgets.Flange"
                if (fqn != null) {
                    fqns.add(fqn);
                }
            }
            // having got all the fqns for each taxon in the taxa, we now need to resolve their
            // hierarchy to find the base class.
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
    // TODO: needs to use domainInfo object
    private String commonBaseClass(Set<String> fqns, String... taxa) {
        if (!fqns.isEmpty()) {
            Set<String> baseClasses = new HashSet<>();
            for (String fqn : fqns) {
                String baseClass = resolveBaseClass(fqn, domainInfo().getClass(fqn).directSubclasses());
                if (baseClass != null) {
                    baseClasses.add(baseClass);
                }
            }
            if (baseClasses.size() > 1) {
                // todo logger.warn
                System.out.println("Multiple leaf classes found in type hierarchy for specified taxa: " + Arrays.toString(taxa) + ". leaf classes are: " + baseClasses);
                return null;
            }
            if (baseClasses.iterator().hasNext()) {
                return baseClasses.iterator().next();
            }
        }
        return null;
    }

    private String resolveBaseClass(String fqn, List<ClassInfo> classInfoList) {
        if (classInfoList.isEmpty()) {
            return fqn;
        }
        if (classInfoList.size() > 1) {
            // todo logger.warn
            System.out.println("More than one class subclasses " + fqn);
            return null; // turn back oh Man - forget thy foolish ways
        }
        ClassInfo classInfo = classInfoList.iterator().next();
        return resolveBaseClass(classInfo.toString(), classInfo.directSubclasses());
    }

    protected abstract Class match(String label);

}
