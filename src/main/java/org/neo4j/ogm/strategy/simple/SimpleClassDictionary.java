package org.neo4j.ogm.strategy.simple;

import org.graphaware.graphmodel.Taxon;
import org.neo4j.ogm.metadata.ClassDictionary;

import java.util.*;

/**
 * Class dictionary. Given a simple class name, will attempt to find the fqns of all classes
 * matching that name in the current classloader's classpath.
 */
public class SimpleClassDictionary implements ClassDictionary {

    private Collection<String> packages;
    private Map<String, List<String>> fqns = new HashMap<>();

    @Override
    public List<String> getFQNs(String simpleName) {

        if (this.packages == null) {
            this.packages = getPackages();
        }

        List<String> qualifiedNames = fqns.get(simpleName);

        if (qualifiedNames == null) {
            qualifiedNames = new ArrayList<>();
            for (String aPackage : packages) {
                try {
                    String fqn = aPackage + "." + simpleName;
                    Class.forName(fqn);
                    qualifiedNames.add(fqn);
                } catch (Exception e) {
                    // Ignore: no such class in this package
                }
            }
            this.fqns.put(simpleName, qualifiedNames);
        }

        return qualifiedNames;
    }

    private Collection<String> getPackages() {
        Set<String> packages = new HashSet<>();
        for (Package aPackage : Package.getPackages()) {
            packages.add(aPackage.getName());
        }
        return packages;
    }

    @Override
    public String determineBaseClass(List<Taxon> taxa) {
        // TODO!
        return null;
    }

//    private Collection<String> getPackages() {
//        String classpath = System.getProperty("java.class.path");
//        return getPackageFromClassPath(classpath);
//    }

//    private static Set<String> getPackageFromClassPath(String classpath) {
//        Set<String> packages = new HashSet<String>();
//        String[] paths = classpath.split(File.pathSeparator);
//        for (String path : paths) {
//            if (path.trim().length() == 0) {
//                continue;
//            } else {
//                File file = new File(path);
//                if (file.exists()) {
//                    String childPath = file.getAbsolutePath();
//                    if (childPath.endsWith(".jar")) {
//                        packages.addAll(ClasspathPackageProvider
//                                .readZipFile(childPath));
//                    } else {
//                        packages.addAll(ClasspathPackageProvider
//                                .readDirectory(childPath));
//                    }
//                }
//            }
//
//        }
//        return packages;
//    }
}
