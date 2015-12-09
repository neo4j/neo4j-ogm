package org.neo4j.ogm.service;

import org.neo4j.ogm.classloader.ClassLoaderResolver;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.config.CompilerConfiguration;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
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
        loadDriver();
    }

    public static void configure(Configuration configuration) {
        Components.configuration = configuration;
        loadDriver();
    }

    private static void loadDriver() {
        driver = DriverService.load(new DriverConfiguration(configuration));
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
    private static void autoConfigure() {

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
                return classPathResource("ogm.properties");
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

    public static void setDriver(Driver driver) {
        Components.driver = driver;
    }
}
