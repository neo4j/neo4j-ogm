package org.neo4j.ogm.spi;

import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.config.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * This class is responsible for loading a Driver using the Service Loader mechanism
 *
 * A Driver can either be loaded explicitly via its class name, or looked up using its registered name
 *
 * To use registered name lookup, the DriverService expects a file to exist on the classpath called
 * "[registeredname].driver.properties", which is the default configuration file for that Driver.
 *
 * If the configuration file is found, the Driver class name is read from a property "class", and the
 * the DriverService attempts to load the named Driver.
 *
 * @author vince
 */
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    /**
     * Using this method, you can load a Driver as a service provider by specifying its
     * fully qualified class name.
     *
     * @param className the fully qualified class name of the required Driver
     * @return the named Driver if found, otherwise throws a ServiceNotFoundException
     */
    public static Driver load(String className) {

        Iterator<Driver> iterator = ServiceLoader.load(Driver.class).iterator();

        while (iterator.hasNext()) {
            try {
                Driver driver = iterator.next();
                if (driver.getClass().getName().equals(className)) {
                    return driver;
                }
            } catch (ServiceConfigurationError sce) {
                logger.warn(sce.getLocalizedMessage() + ", reason: " + sce.getCause());
            }
        }

        throw new ServiceNotFoundException(className);

    }

    /**
     * Using this method you can load a Driver using its registered name.
     * The registered name for a driver is specified by the "class" property in the Driver's config file,
     * and the value should be the fully qualified class name of the implementing Driver class.
     *
     * @param registeredName the Driver's registered name
     * @return the required Driver if found, otherwise throws a ServiceNotFoundException
     */
    public static Driver lookup(String registeredName) {

        ServiceConfiguration config = new ServiceConfiguration(registeredName + ".driver.properties");
        String fqn = (String) config.getConfig("class");
        if (fqn != null) {
            return load(fqn);
        }

        throw new ServiceNotFoundException(registeredName);
    }

}
