package org.neo4j.ogm.session;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.driver.Driver;

/**
 * @author Mark Angrish
 */
public class Neo4jBoltSession extends Neo4jSession {

	private String bookmark;

	public Neo4jBoltSession(MetaData metaData, Driver driver) {
		super(metaData, driver);
	}

	public String getLastBookmark() {
		return bookmark;
	}

	public void lastBookmark(String bookmark) {
		this.bookmark = bookmark;
	}
}
