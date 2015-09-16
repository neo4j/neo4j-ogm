package org.neo4j.ogm.driver;

import org.neo4j.ogm.driver.bolt.BoltDriver;
import org.neo4j.ogm.driver.embedded.EmbeddedDriver;
import org.neo4j.ogm.driver.http.HttpDriver;

/**
 * @author vince
 */
public abstract class Drivers {

    public static final Driver HTTP = new HttpDriver();
    public static final Driver BOLT = new BoltDriver();
    public static final Driver EMBEDDED = new EmbeddedDriver();

    public static Driver DEFAULT = HTTP;

}
