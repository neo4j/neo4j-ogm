package org.neo4j.ogm.spi;

import org.neo4j.ogm.driver.api.driver.Driver;
import org.neo4j.ogm.driver.impl.driver.DriverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * This class is responsible for loading a Driver using the service loader mechanism
 *
 * @author vince
 */
public abstract class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    /**
     * Using this method, you can load a Driver as a service provider by specifying its
     * fully qualified classname.
     * @param name
     * @return
     */
    public static Driver load(String name) {

        Iterator<Driver> iterator = ServiceLoader.load(Driver.class).iterator();

        while (iterator.hasNext()) {
            try {
                Driver driver = iterator.next();
                if (driver.getClass().getName().equals(name)) {
                    return driver;
                }
            } catch (ServiceConfigurationError sce) {
                logger.warn(sce.getLocalizedMessage() + ", reason: " + sce.getCause());
            }
        }

        throw new DriverNotFoundException("Could not obtain driver: " + name);

    }

    /**
     * Using this method you can load a driver using its internal driver name.
     * The internal name for a driver by convention forms part of the config file name for a specific driver.
     *
     * @param name the driver name
     * @return
     */
    public static Driver lookup(String name) {

        DriverConfig config = new DriverConfig("driver.properties." + name);
        String fqn = (String) config.getConfig("class");
        if (fqn != null) {
            return load(fqn);
        }

        throw new DriverNotFoundException(name);
    }

}
