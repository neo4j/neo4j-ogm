package org.neo4j.ogm.metadata;

public class PropertyMapping {

    private String propertyName;

    void writeToObject(Object target) {
        /*
         * this class will know the field it's interested in on the given target option because it's bound to a property
         * how it sets it will be down to the property setter strategy it's given.
         */
    }

}
