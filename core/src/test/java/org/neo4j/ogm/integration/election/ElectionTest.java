package org.neo4j.ogm.integration.election;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.domain.election.Candidate;
import org.neo4j.ogm.domain.election.Voter;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.IntegrationTestRule;

import java.io.IOException;

import static org.junit.Assert.*;

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
        session = sessionFactory.openSession();
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


    @Test
    public void shouldAllowVoterToChangeHerMind() {

        Candidate a = new Candidate("A");
        Candidate b = new Candidate("B");
        Voter v = new Voter("V");

        v.candidateVotedFor = b;

        session.save(a);
        session.save(v);

        MappingContext context = ((Neo4jSession) session).context();

        assertTrue(context.isRegisteredRelationship(new MappedRelationship(v.getId(),"CANDIDATE_VOTED_FOR",  b.getId(), Voter.class, Candidate.class)));
        session.clear();

        System.out.println("reloading objects");
        a = session.load(Candidate.class, a.getId());
        v = session.load(Voter.class, v.getId());

        assertEquals(b.getId(), v.candidateVotedFor.getId());

        assertTrue(context.isRegisteredRelationship(new MappedRelationship(v.getId(),"CANDIDATE_VOTED_FOR",  b.getId(), Voter.class, Candidate.class)));


        v.candidateVotedFor = a;

        session.save(v);

        System.out.println("reloading objects");
        session.clear();
        session.load(Candidate.class, b.getId());
        session.load(Voter.class, v.getId());

        assertEquals(a.getId(), v.candidateVotedFor.getId());

        assertTrue(context.isRegisteredRelationship(new MappedRelationship(v.getId(),"CANDIDATE_VOTED_FOR",  a.getId(), Voter.class, Candidate.class)));
        assertFalse(context.isRegisteredRelationship(new MappedRelationship(v.getId(), "CANDIDATE_VOTED_FOR", b.getId(), Voter.class, Candidate.class)));

        session.clear();



    }

}
