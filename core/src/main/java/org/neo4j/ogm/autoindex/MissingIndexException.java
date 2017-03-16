package org.neo4j.ogm.autoindex;

/**
 * Exception representing missing indexes and/or constraints.
 *
 * @author Mark Angrish
 */
class MissingIndexException extends RuntimeException {

	MissingIndexException(String message) {
		super(message);
	}
}
