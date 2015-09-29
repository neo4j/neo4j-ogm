package org.neo4j.ogm.integration.election;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.election.Candidate;
import org.neo4j.ogm.domain.election.Voter;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.IntegrationTestRule;

import java.io.IOException;

/**
 * These tests assert that we can create loop edges in the graph, to support use cases
 * where for example, in an election, a candidate (who is also a voter) is able to vote
 * for herself.
 *
 * @See DATAGRAPH-689
 *
 * @author vince
 */
public class ElectionTest {

    @Rule
    public IntegrationTestRule testServer = new IntegrationTestRule();

    private static final SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.election");

    private Session session;

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession(testServer.driver());
    }

    private GraphDatabaseService getDatabase() {
        return testServer.getGraphDatabaseService();
    }

    @Test
    public void shouldAllowACandidateToVoteForHerself() {

        Candidate candidate = new Candidate("Hilary Clinton");
        candidate.candidateVotedFor = candidate;

        session.save(candidate);

        assertNotNull(candidate.getId());
        assertNotNull(candidate.candidateVotedFor.getId());
        assertEquals(candidate.getId(), candidate.candidateVotedFor.getId());

        session.clear();

        Long voterId = candidate.getId();

        Voter voter = session.load(Voter.class, voterId);

        assertNotNull(voter.getId());
        assertNotNull(voter.candidateVotedFor.getId());
        assertEquals(voter.getId(), voter.candidateVotedFor.getId());

    }

    @Test
    public void shouldAllowASelfReferenceToBeSavedFromTheReferredSide() {

        Candidate candidate = new Candidate("Hilary Clinton");
        candidate.candidateVotedFor = candidate;

        session.save(candidate.candidateVotedFor);

        session.clear();

        Long voterId = candidate.candidateVotedFor.getId();

        Voter voter = session.load(Voter.class, voterId);

        assertNotNull(voter.getId());
        assertNotNull(voter.candidateVotedFor.getId());
        assertEquals(voter.getId(), voter.candidateVotedFor.getId());

    }



}
