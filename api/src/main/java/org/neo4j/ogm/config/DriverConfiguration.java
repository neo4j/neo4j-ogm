package org.neo4j.ogm.config;

import org.neo4j.ogm.authentication.Credentials;
import org.neo4j.ogm.authentication.UsernamePasswordCredentials;

import java.net.URL;

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
        try { // if this URI is a genuine resource, see if it has an embedded userinfo and set credentials accordingly
            URL url = new URL(uri);
            String userInfo = url.getUserInfo();
            if (userInfo != null) {
                String[] userPass = userInfo.split(":");
                setCredentials(userPass[0], userPass[1]);
            }
        } catch (Exception e) {
            ; // do nothing here. user not obliged to supply a URL, or to pass in credentials
        }
    }

    public void setCredentials(Credentials credentials) {
        configuration.set(CREDENTIALS, credentials);
    }

    public void setCredentials(String username, String password) {
        configuration.set(CREDENTIALS, new UsernamePasswordCredentials(username, password));
    }

    public Credentials getCredentials() {
        if (configuration.get(CREDENTIALS) == null) {
            setURI((String) configuration.get(URI)); // set from the URI?
        }
        return (Credentials) configuration.get(CREDENTIALS);
    }

    public String getURI() {
        return (String)  configuration.get(URI);
    }

    public String getDriverClassName() {
        return (String) configuration.get(DRIVER);
    }

}
