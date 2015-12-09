package org.neo4j.ogm.config;

import org.neo4j.ogm.authentication.Credentials;
import org.neo4j.ogm.authentication.UsernamePasswordCredentials;

/**
 * 
 * A wrapper class for a generic {@link Configuration} that exposes the configuration for a Driver via
 * concrete methods.
 * 
 * @author vince
 */
public class DriverConfiguration {

    public static final String DRIVER = "driver";
    public static final String CREDENTIALS = "credentials";
    public static final String URI = "URI";

    private final Configuration configuration;

    public DriverConfiguration() {
        this.configuration = new Configuration();
    }

    public DriverConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setDriverClassName(String driverClassName) {
        configuration.set(DRIVER, driverClassName);
    }

    public void setURI(String uri) {
        configuration.set(URI, uri);
    }

    public void setCredentials(Credentials credentials) {
        configuration.set(CREDENTIALS, credentials);
    }

    public void setCredentials(String username, String password) {
        configuration.set(CREDENTIALS, new UsernamePasswordCredentials(username, password));
    }

    public Credentials getCredentials() {
        return (Credentials) configuration.get(CREDENTIALS);
    }

    public String getURI() {
        return (String)  configuration.get(URI);
    }

    public String getDriverClassName() {
        return (String) configuration.get(DRIVER);
    }

}
