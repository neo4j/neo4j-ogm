package org.neo4j.ogm.config;

import java.util.Properties;

/**
 * Provides access to a configuration store and exposes configuration values in bulk {@link Properties} format.
 * Allows users to define and load custom property formats (e.g. YAML, HOGAN etc.) that are not supported in the core OGM API.
 *
 * @author Mark Angrish
 */
public interface ConfigurationSource {

    /**
     * Get configuration set for this source in a form of {@link Properties}.
     *
     * @return configuration set .
     */
    Properties properties();
}
