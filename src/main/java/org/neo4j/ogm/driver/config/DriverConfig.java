package org.neo4j.ogm.driver.config;

import org.neo4j.ogm.metadata.classloader.ClassLoaderResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author vince
 */
public class DriverConfig {

    private final Logger logger = LoggerFactory.getLogger(DriverConfig.class);

    private final Map<String, Object> config = new HashMap();

    public DriverConfig() {}

    public DriverConfig(String propertiesFilename) {
        configure(propertiesFilename);
    }

    public void setConfig(String key, Object value) {
        config.put(key, value);
    }

    public Object getConfig(String key) {
        return config.get(key);
    }

    private void configure(String propertiesFileName) {

        ClassLoader classLoader = ClassLoaderResolver.resolve();

        try(InputStream is = classLoader.getResourceAsStream(propertiesFileName)) {
            Properties properties = new Properties();
            properties.load(is);
            Enumeration propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                config.put(propertyName, properties.getProperty(propertyName));
            }
        } catch (Exception e) {
            logger.warn("Could not load " + propertiesFileName);
        }
    }


}
