package org.neo4j.ogm.spi;

/**
 * @author vince
 */
public class DriverNotFoundException extends RuntimeException {

    public DriverNotFoundException(String s) {
        super(s);
    }
}
