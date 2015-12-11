package org.neo4j.ogm.service;

import org.neo4j.ogm.classloader.ClassLoaderResolver;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.config.CompilerConfiguration;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This class is responsible for ensuring that the various pluggable components
 * required by the OGM can be loaded.
 *
 * The Components class can be explicitly configured via an {@link Configuration} instance.
 *
 * If no explicit configuration is supplied, the class will attempt to auto-configure.
 *
 * Auto-configuration is accomplished using a properties file. By default, this file
 * is called "ogm.properties" and it must be available on the class path.
 *
 * You can supply a different configuration properties file, by specifying a system property
 * "OGM_CONFIG_FILE" that refers to the configuration file you want to use. Your alternative
 * configuration file must be on the class path.
 *
 * The properties file should contain the desired configuration values for each of the
 * various components - Driver, Compiler, etc. Please refer to the relevant configuration
 * for each of these.
 *
 * @author vince
 */
public class Components {

    private Components() {}

    private static final Logger logger = LoggerFactory.getLogger(Components.class);

    private static Configuration configuration = new Configuration();
    private static Driver driver;

    static {
        autoConfigure();
    }

    public static void configure(Configuration configuration) {
        Components.configuration = configuration;
        loadDriver();
    }

    private static void loadDriver() {
        setDriver (DriverService.load(new DriverConfiguration(configuration)));
    }

    private static Compiler loadCompiler() {
        return CompilerService.load(new CompilerConfiguration(configuration));
    }

    // only one instance of the driver exists for the lifetime of the application
    public static Driver driver() {
        return driver;
    }

    // new instance of the compiler is returned every time
    public static Compiler compiler() {
        return loadCompiler();
    }

    /**
     * The OGM Components can be auto-configured from a properties file, "ogm.properties", or
     * a similar configuration file, specified by a system property or environment variable called "OGM_CONFIG".
     *
     * If an auto-configure properties file is not available by any of these means, the Components class should be configured
     * by passing in a Configuration object to the configure method.
     */
    public static void autoConfigure() {

        try(InputStream is = configurationFile()) {

            Properties properties = new Properties();
            properties.load(is);
            Enumeration propertyNames = properties.propertyNames();

            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                configuration.set(propertyName, properties.getProperty(propertyName));
            }
        } catch (Exception e) {
            logger.warn("Could not autoconfigure the OGM");
        }
        loadDriver();
    }

    private static InputStream configurationFile() throws Exception {
        String configFileName;
        configFileName = System.getenv("ogm.properties");

        if (configFileName == null) {
            configFileName = System.getProperty("ogm.properties");
            if (configFileName == null) {
                return classPathResource("ogm.properties");
            }
        }
        // load the config from the user-specified file
        return classPathResource(configFileName);
    }

    private static InputStream classPathResource(String name) {
        System.out.println("Configuring from: " + name);
        return ClassLoaderResolver.resolve().getResourceAsStream(name);
    }

    public static void setDriver(Driver driver) {
        System.out.println(" *** Setting driver to: " + driver.getClass().getName());
        Components.driver = driver;
    }

    public static double neo4jVersion() {
        String neo4jVersion = (String) configuration.get("neo4j.version");
        if (neo4jVersion != null) {
            try {
                return new Double(neo4jVersion);
            } catch (NumberFormatException nfe) {
                logger.warn("Configuration property 'neo4j.version' is not in the correct form: expected something like '2.3'");
            }
        }
        return 9.9; // unknown version
    }
}
