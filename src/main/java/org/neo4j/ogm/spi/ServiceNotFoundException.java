package org.neo4j.ogm.spi;

/**
 * @author vince
 */
public class ServiceNotFoundException extends RuntimeException {

    public ServiceNotFoundException(String s) {
        super(s);
    }
}
