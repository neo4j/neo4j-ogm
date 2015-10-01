package org.neo4j.ogm.driver.api.request;

import java.util.Map;

/**
 * @author vince
 */
public interface Statement {

    public String getStatement();

    public Map<String, Object> getParameters();
}
