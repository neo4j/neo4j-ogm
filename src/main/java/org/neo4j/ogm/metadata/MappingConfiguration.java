package org.neo4j.ogm.metadata;

import org.neo4j.ogm.metadata.dictionary.PersistentFieldDictionary;

/**
 * Provides the configuration for the object-graph mapping, including how instances of particular objects should be written
 * to and read from the graph.
 */
public interface MappingConfiguration {

    /*
     * The aim is for this to provide answers to questions that the mapper will need to ask in order to do its work.
     * This class doesn't know about how the mapping information got here, so, for example, reading @Label isn't done by this
     *
     * Do I need to make a distinction between the implementation of these objects and the way the metadata is gathered?
     * currently, an impl'n of MappingConfiguration will need to do the lookup of mapping metadata for a given class, which is
     * functionality, and also provide the object creation strategy, which is configuration.
     *
     * Given the class is called "configuration", I reckon this may actually be a sub-standard design.  Let's have a look at how
     * we'd build one of these.
     */

    // I fancy this metadata will also be referenced by all the "do stuff" objects that get returned from this class
    PersistentFieldDictionary findMappingMetadataForType(Class<?> typeToMap);

    // typically reflective with default constructor or some other ad-hoc pattern
    // could also create objects from relationship as well
    ObjectFactory provideObjectFactory();

    // Decides how data actually gets written to the object.
    // Java Beans setter-driven or field-driven implementation variations, maybe?
    // in fact, could we potentially need different strategies for different target types!?
//    ThingThatCanWriteValuesToObjectProperties provideWritingToObjectStrategy();

}
