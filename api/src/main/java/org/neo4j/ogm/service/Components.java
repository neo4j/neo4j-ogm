package org.neo4j.ogm.service;

import org.neo4j.ogm.classloader.ClassLoaderResolver;
import org.neo4j.ogm.driver.Driver;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * This class is responsible for ensuring that the various pluggable components
 * required by the OGM can be found when required. The configuration is loaded
 * in a static initialiser block, but the individual components are not statically
 * loaded, they are provided "on demand" when requested.
 *
 *
 *
 * @author vince
 */
public class Components {

    private Components() {};

    private static final Map<String, String> config = new HashMap();

    static {
        configure();
    }

    public static Driver driver() {
        return DriverService.lookup(config.get(Component.DRIVER.toString()));
    }


    public static org.neo4j.ogm.compiler.Compiler compiler() {
        return CompilerService.lookup(config.get(Component.COMPILER.toString()));
    }

    private static void configure() {

        try(InputStream is = configurationFile()) {

            Properties properties = new Properties();
            properties.load(is);
            Enumeration propertyNames = properties.propertyNames();

            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                config.put(propertyName, properties.getProperty(propertyName));
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not configure OGM", e);
        }
    }

    /*
     * looks for an environment variable OGM_CONFIG,
     * looks for system property setting OGM_CONFIG
     * or returns "default.ogm.properties"
     *
     * @return the name of the configuration file containing the configuration settings for the OGM
     *
     */
    private static InputStream configurationFile() throws Exception {
        String configFileName;
        configFileName = System.getenv("OGM_CONFIG");
        if (configFileName == null) {
            configFileName = System.getProperty("OGM_CONFIG");
            if (configFileName == null) {
                return classPathResource("default.ogm.properties");
            }
        }
        return fileSystemResource(configFileName);
    }

    private static InputStream fileSystemResource(String name) throws Exception {
        return new FileInputStream(name);

    }

    private static InputStream classPathResource(String name) {
        return ClassLoaderResolver.resolve().getResourceAsStream(name);
    }
}
