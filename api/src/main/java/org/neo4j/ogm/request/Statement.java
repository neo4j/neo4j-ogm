package org.neo4j.ogm.request;

import java.util.Map;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public interface Statement {

    String getStatement();

    Map<String, Object> getParameters();

    String[] getResultDataContents();

    boolean isIncludeStats();
}
