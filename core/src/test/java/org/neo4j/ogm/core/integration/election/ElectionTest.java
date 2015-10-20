package org.neo4j.ogm.core.integration.election;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.core.domain.election.Candidate;
import org.neo4j.ogm.core.domain.election.Voter;
import org.neo4j.ogm.core.testutil.IntegrationTestRule;
import org.neo4j.ogm.core.domain.election.Candidate;
import org.neo4j.ogm.core.domain.election.Voter;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.core.testutil.IntegrationTestRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
