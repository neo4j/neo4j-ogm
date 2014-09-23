package org.neo4j.ogm.metadata;

/**
 * Provides the configuration for the object-graph mapping, including how instances of particular objects should be written
 * to and read from the graph.
 */
public interface MappingConfiguration {

    /*
     * The aim is for this to provide answers to questions that the mapper will need to ask in order to do its work.
     * This class doesn't know about how the mapping information got here, so, for example, reading @Label isn't done by this
     */

    // I fancy this metadata will also be referenced by all the "do stuff" objects that get returned from this class
    MappingMetadata findMappingMetadataForType(Class<?> typeToMap);

    // typically reflective with default constructor or some other ad-hoc pattern
    // could also create objects from relationship as well
    ObjectCreator provideObjectCreator();

    // Decides how data actually gets written to the object.
    // Java Beans setter-driven or field-driven implementation variations, maybe?
    // in fact, could we potentially need different strategies for different target types!?
//    ThingThatCanWriteValuesToObjectProperties provideWritingToObjectStrategy();

}
