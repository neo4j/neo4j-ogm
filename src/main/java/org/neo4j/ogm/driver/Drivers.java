package org.neo4j.ogm.driver;

/**
 * @author vince
 */
public abstract class Drivers {

    public static Driver HTTP = new HttpClientDriver();
    public static Driver DEFAULT = HTTP;

}
