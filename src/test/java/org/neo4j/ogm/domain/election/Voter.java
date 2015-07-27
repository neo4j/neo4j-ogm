package org.neo4j.ogm.domain.election;

/**
 * @author vince
 */
public class Voter extends Entity {

    public Voter() {
        super();
    }

    public Voter(String s) {
        super(s);
    }

    public Candidate candidateVotedFor;

}
