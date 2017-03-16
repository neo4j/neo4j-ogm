package org.neo4j.ogm.autoindex;

/**
 * Exception representing missing indexes and/or constraints.
 *
 * @author Mark Angrish
 */
public class MissingIndexException extends RuntimeException {

	public MissingIndexException(String message) {
		super(message);
	}
}
