package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.Taxon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Super-simple implementation of {@link ClassDictionary} backed my a map.  This will probably become obsolete very soon.
 */
public class MapBasedClassDictionary implements ClassDictionary {

    private final Map<String, String> classMap = new HashMap<>();

    /**
     * @param classMap A mapping between taxon names and fully-qualified class names
     */
    public MapBasedClassDictionary(Map<String, String> classMap) {
        this.classMap.putAll(classMap);
    }

    @Override
    public String determineBaseClass(List<Taxon> taxa) {
        for (Taxon taxon : taxa) {
            if (this.classMap.containsKey(taxon.getName().toString())) {
                return this.classMap.get(taxon.getName().toString());
            }
        }
        return null;
    }

    @Override
    public List<String> getFQNs(String simpleName) {
        // todo:
        throw new RuntimeException("Not implemented");
    }

}
