package org.neo4j.ogm.config;

import org.neo4j.ogm.classloader.ClassLoaderResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * A generic configuration class that can be set up programmatically
 * or via a properties file.
 *
 * @author vince
 */
public class Configuration {

    private final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private final Map<String, Object> config = new HashMap<>();

    public Configuration() {}

    public Configuration(String propertiesFilename) {
        configure(propertiesFilename);
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public Object get(String key) {
        return config.get(key);
    }

    private void configure(String propertiesFileName) {

        try(InputStream is = ClassLoaderResolver.resolve().getResourceAsStream(propertiesFileName)) {

            Properties properties = new Properties();
            properties.load(is);
            Enumeration propertyNames = properties.propertyNames();

            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                config.put(propertyName, properties.getProperty(propertyName));
            }
        } catch (Exception e) {
            logger.warn("Could not load {}",propertiesFileName);
        }
    }


}
