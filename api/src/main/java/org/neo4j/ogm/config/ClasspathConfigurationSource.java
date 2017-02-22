package org.neo4j.ogm.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * {@link ConfigurationSource} reading configuration from classpath files.
 *
 * @author Mark Angrish
 */
public class ClasspathConfigurationSource implements ConfigurationSource {

    private Properties properties = new Properties();

    public ClasspathConfigurationSource(String propertiesFileName) {

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName)) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file: " + propertiesFileName, e);
        }
    }

    @Override
    public Properties properties() {
        return this.properties;
    }
}
