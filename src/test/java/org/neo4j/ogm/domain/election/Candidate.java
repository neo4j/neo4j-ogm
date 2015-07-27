package org.neo4j.ogm.domain.election;

/**
 * A Candidate is a voter and therefore may theoretically vote for herself.
 *
 * This will result in a loop edge in the graph
 *
 * @See DATAGRAPH-689
 *
 * @author vince
 */
public class Candidate extends Voter {

    public Candidate() {
        super();
    }

    public Candidate(String s) {
        super(s);
    }
}
