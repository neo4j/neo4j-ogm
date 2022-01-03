/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.persistence.examples.election;

import static org.assertj.core.api.Assertions.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.context.MappedRelationship;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.domain.election.Candidate;
import org.neo4j.ogm.domain.election.Voter;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * These tests assert that we can create loop edges in the graph, to support use cases
 * where for example, in an election, a candidate (who is also a voter) is able to vote
 * for herself.
 *
 * @author vince
 * @See DATAGRAPH-689
 */
public class ElectionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.election");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    @Test
    public void shouldAllowACandidateToVoteForHerself() {

        Candidate candidate = new Candidate("Hilary Clinton");
        candidate.candidateVotedFor = candidate;

        session.save(candidate);

        assertThat(candidate.getId()).isNotNull();
        assertThat(candidate.candidateVotedFor.getId()).isNotNull();
        assertThat(candidate.candidateVotedFor.getId()).isEqualTo(candidate.getId());

        session.clear();

        Long voterId = candidate.getId();

        Voter voter = session.load(Voter.class, voterId);

        assertThat(voter.getId()).isNotNull();
        assertThat(voter.candidateVotedFor.getId()).isNotNull();
        assertThat(voter.candidateVotedFor.getId()).isEqualTo(voter.getId());
    }

    @Test
    public void shouldAllowASelfReferenceToBeSavedFromTheReferredSide() {

        Candidate candidate = new Candidate("Hilary Clinton");
        candidate.candidateVotedFor = candidate;

        session.save(candidate.candidateVotedFor);

        session.clear();

        Long voterId = candidate.candidateVotedFor.getId();

        Voter voter = session.load(Voter.class, voterId);

        assertThat(voter.getId()).isNotNull();
        assertThat(voter.candidateVotedFor.getId()).isNotNull();
        assertThat(voter.candidateVotedFor.getId()).isEqualTo(voter.getId());
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

        assertThat(context.containsRelationship(
            new MappedRelationship(v.getId(), "CANDIDATE_VOTED_FOR", b.getId(), null, Voter.class, Candidate.class)))
            .isTrue();
        session.clear();

        a = session.load(Candidate.class, a.getId());
        v = session.load(Voter.class, v.getId());

        assertThat(v.candidateVotedFor.getId()).isEqualTo(b.getId());

        assertThat(context.containsRelationship(
            new MappedRelationship(v.getId(), "CANDIDATE_VOTED_FOR", b.getId(), null, Voter.class, Candidate.class)))
            .isTrue();

        v.candidateVotedFor = a;

        session.save(v);

        session.clear();
        session.load(Candidate.class, b.getId());
        session.load(Voter.class, v.getId());

        assertThat(v.candidateVotedFor.getId()).isEqualTo(a.getId());

        assertThat(context.containsRelationship(
            new MappedRelationship(v.getId(), "CANDIDATE_VOTED_FOR", a.getId(),null, Voter.class, Candidate.class)))
            .isTrue();
        assertThat(context.containsRelationship(
            new MappedRelationship(v.getId(), "CANDIDATE_VOTED_FOR", b.getId(), null, Voter.class, Candidate.class)))
            .isFalse();

        session.clear();
    }
}
