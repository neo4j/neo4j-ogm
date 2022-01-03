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
package org.neo4j.ogm.persistence.relationships;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.entityMapping.Movie;
import org.neo4j.ogm.domain.entityMapping.Person;
import org.neo4j.ogm.domain.entityMapping.Rating;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class MultipleRelationshipsTest extends TestContainersTestBase {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.entityMapping").openSession();
        session.purgeDatabase();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
        session.clear();
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void shouldMapFromGraphToEntitiesCorrectly() {
        session.query("create (_5:`PersonX` {`name`:\"Jim\"})\n" +
            "create (_6:`PersonX` {`name`:\"Mary\"})\n" +
            "create (_7:`PersonX` {`name`:\"Bill\"})\n" +
            "create (_8:`Movie` {`name`:\"Die Hard\"})\n" +
            "create (_9:`Movie` {`name`:\"The Matrix\"})\n" +
            "create (_5)-[:`FOLLOWS`]->(_6)\n" +
            "create (_5)-[:`LIKES`]->(_6)\n" +
            "create (_5)-[:`RATED` {`value`:4}]->(_9)\n" +
            "create (_5)-[:`RATED` {`value`:5}]->(_8)\n" +
            "create (_6)-[:`FOLLOWS`]->(_5)\n" +
            "create (_6)-[:`FOLLOWS`]->(_7)\n" +
            "create (_6)-[:`LIKES`]->(_5)\n" +
            "create (_6)-[:`RATED` {`value`:5}]->(_8)\n" +
            "create (_7)-[:`FOLLOWS`]->(_5)\n" +
            "create (_7)-[:`LIKES`]->(_6)\n" +
            "create (_7)-[:`RATED` {`value`:4}]->(_9)\n" +
            "create (_7)-[:`RATED` {`value`:5}]->(_9)\n" +
            ";\n", Collections.EMPTY_MAP);
        Person jim = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "Jim")).iterator()
            .next();
        assertThat(jim.movieRatings).hasSize(2);
        assertThat(jim.peopleILike).hasSize(1);
        assertThat(jim.peopleWhoLikeMe).hasSize(1);
        assertThat(jim.peopleIFollow).hasSize(1);
        assertThat(jim.peopleWhoFollowMe).hasSize(2);
        assertThat(jim.peopleILike.get(0).name).isEqualTo("Mary");

        Person bill = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "Bill")).iterator()
            .next();
        assertThat(bill.movieRatings).hasSize(2);
        assertThat(bill.peopleILike).hasSize(1);
        assertThat(bill.peopleWhoLikeMe).isEmpty();
        assertThat(bill.peopleIFollow).hasSize(1);
        assertThat(bill.peopleWhoFollowMe).hasSize(1);

        Person mary = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "Mary")).iterator()
            .next();
        Movie dieHard = session.loadAll(Movie.class, new Filter("name", ComparisonOperator.EQUALS, "Die Hard"))
            .iterator().next();
        Movie matrix = session.loadAll(Movie.class, new Filter("name", ComparisonOperator.EQUALS, "The Matrix"))
            .iterator().next();
        assertThat(mary.movieRatings).hasSize(1);
        assertThat(mary.peopleILike).hasSize(1);
        assertThat(mary.peopleWhoLikeMe).hasSize(2);
        assertThat(mary.peopleIFollow).hasSize(2);
        assertThat(mary.peopleWhoFollowMe).hasSize(1);

        assertThat(matrix.ratings).hasSize(3);
        assertThat(dieHard.ratings).hasSize(2);
    }

    /**
     * @see DATAGRAPH-690
     */
    @Test
    public void shouldCreateGraphProperly() {
        Person jim = new Person();
        Person mary = new Person();
        Person bill = new Person();

        jim.name = "Jim";
        mary.name = "Mary";
        bill.name = "Bill";

        bill.peopleIFollow.add(jim);
        bill.peopleILike.add(mary);
        bill.peopleWhoFollowMe.add(mary);

        mary.peopleIFollow.add(bill);
        mary.peopleIFollow.add(jim);
        mary.peopleILike.add(jim);
        mary.peopleWhoLikeMe.add(bill);
        mary.peopleWhoFollowMe.add(jim);
        mary.peopleWhoLikeMe.add(jim);

        jim.peopleIFollow.add(mary);
        jim.peopleILike.add(mary);
        jim.peopleWhoFollowMe.add(bill);
        jim.peopleWhoFollowMe.add(mary);
        jim.peopleWhoLikeMe.add(mary);
        session.save(jim);

        Movie matrix = new Movie();
        matrix.name = "The Matrix";

        Movie dieHard = new Movie();
        dieHard.name = "Die Hard";

        Rating ratingOne = Rating.create(bill, matrix, 4);
        Rating ratingTwo = Rating.create(bill, matrix, 5);
        Rating ratingThree = Rating.create(jim, matrix, 3);
        Rating ratingFour = Rating.create(jim, dieHard, 5);
        Rating ratingFive = Rating.create(mary, dieHard, 5);

        bill.movieRatings.add(ratingOne);
        matrix.ratings.add(ratingOne);

        bill.movieRatings.add(ratingTwo);
        matrix.ratings.add(ratingTwo);

        jim.movieRatings.add(ratingThree);
        matrix.ratings.add(ratingThree);

        jim.movieRatings.add(ratingFour);
        dieHard.ratings.add(ratingFour);

        mary.movieRatings.add(ratingFive);
        dieHard.ratings.add(ratingFive);

        session.save(bill);
        session.save(jim);
        session.save(mary);

        Long idDieHard = dieHard.id;
        Long idMatrix = matrix.id;
        Long idJim = jim.id;
        Long idBill = bill.id;
        Long idMary = mary.id;

        session.clear();

        jim = session.load(Person.class, idJim);
        assertThat(jim.movieRatings).hasSize(2);
        assertThat(jim.peopleILike).hasSize(1);
        assertThat(jim.peopleWhoLikeMe).hasSize(1);
        assertThat(jim.peopleIFollow).hasSize(1);
        assertThat(jim.peopleWhoFollowMe).hasSize(2);

        bill = session.load(Person.class, idBill);
        assertThat(bill.movieRatings).hasSize(2);
        assertThat(bill.peopleILike).hasSize(1);
        assertThat(bill.peopleWhoLikeMe).isEmpty();
        assertThat(bill.peopleIFollow).hasSize(1);
        assertThat(bill.peopleWhoFollowMe).hasSize(1);

        mary = session.load(Person.class, idMary);
        assertThat(mary.movieRatings).hasSize(1);
        assertThat(mary.peopleILike).hasSize(1);
        assertThat(mary.peopleWhoLikeMe).hasSize(2);
        assertThat(mary.peopleIFollow).hasSize(2);
        assertThat(mary.peopleWhoFollowMe).hasSize(1);

        dieHard = session.load(Movie.class, idDieHard);
        matrix = session.load(Movie.class, idMatrix);
        assertThat(matrix.ratings).hasSize(3);
        assertThat(dieHard.ratings).hasSize(2);

        Person bob = new Person();
        bob.name = "Bob";

        Rating ratingSix = Rating.create(bob, matrix, 4);
        Rating ratingSeven = Rating.create(bob, matrix, 5);

        bob.movieRatings.add(ratingSix);
        bob.movieRatings.add(ratingSeven);

        session.save(bob);

        session.clear();

        bob = session.load(Person.class, bob.id);
        assertThat(bob.movieRatings).hasSize(2);
    }
}
