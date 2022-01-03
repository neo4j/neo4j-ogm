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
package org.neo4j.ogm.persistence.session.capability;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.neo4j.ogm.domain.gh651.PersistentCategory;
import org.neo4j.ogm.domain.gh651.SomeRelationshipEntity;
import org.neo4j.ogm.domain.gh651.SomeEntity;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.request.GraphRowListModelRequest;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.delegates.LoadByIdsDelegate;
import org.neo4j.ogm.session.delegates.LoadByTypeDelegate;
import org.neo4j.ogm.session.delegates.LoadOneDelegate;
import org.neo4j.ogm.session.transaction.support.TransactionalUnitOfWork;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * This test is to ensure that we correctly add the labels from class hierachies to queries, depending on whether we
 * match all or one of a type by id or all of a type.
 * <p>
 * A type can be either represent a node or relationship entity
 *
 * @author Michael J. Simons
 */
@RunWith(MockitoJUnitRunner.class)
public class StrictQueryingTest {

    private final MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh651");

    @Spy
    Neo4jSession neo4jSession = new Neo4jSession(metaData, true, new AbstractConfigurableDriver() {
        @Override protected String getTypeSystemName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Function<TransactionManager, BiFunction<Transaction.Type, Iterable<String>, Transaction>> getTransactionFactorySupplier() {
            return transactionManager -> null;
        }

        @Override
        public void close() {

        }

        @Override
        public Request request(Transaction transaction) {
            return null;
        }
    });

    @Mock
    Request request;

    @Before
    public void prepareMocks() {
        doReturn(metaData).when(neo4jSession).metaData();
        doReturn(request).when(neo4jSession).requestHandler();
        doAnswer(invocation -> ((TransactionalUnitOfWork) invocation.getArgument(0)).doInTransaction())
            .when(neo4jSession).doInTransaction(any(TransactionalUnitOfWork.class), any(Transaction.Type.class));
        Response<GraphModel> response = new Response<GraphModel>() {
            @Override
            public GraphModel next() {
                return null;
            }

            @Override
            public void close() {
            }

            @Override public String[] columns() {
                return new String[0];
            }
        };
        doReturn(response).when(request).execute(Mockito.any(GraphModelRequest.class));
        doReturn(response).when(request).execute(Mockito.any(GraphRowListModelRequest.class));
    }

    @Test // GH-651
    public void shouldUseOnlyOneLabelForStandardEntity() {

        LoadByIdsDelegate delegate = new LoadByIdsDelegate(neo4jSession);
        delegate.loadAll(SomeEntity.class, Arrays.asList(1L, 2L));
        ArgumentCaptor<GraphModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).isEqualTo("MATCH (n:`SomeEntity`) WHERE ID(n) IN $ids WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
    }

    @Test // GH-651
    public void shouldUseOnlyOneLabelForOneStandardEntity() {

        LoadOneDelegate delegate = new LoadOneDelegate(neo4jSession);
        delegate.load(SomeEntity.class, 4711L);
        ArgumentCaptor<GraphModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).isEqualTo("MATCH (n:`SomeEntity`) WHERE ID(n) = $id WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
    }

    @Test // GH-651
    public void shouldUseOnlyOneLabelForAllStandardEntities() {

        LoadByTypeDelegate delegate = new LoadByTypeDelegate(neo4jSession);
        delegate.loadAll(SomeEntity.class);
        ArgumentCaptor<GraphModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).isEqualTo("MATCH (n:`SomeEntity`) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
    }

    @Test // GH-651
    public void shouldUseAllLabelsInInheritanceScenario() {

        LoadByIdsDelegate delegate = new LoadByIdsDelegate(neo4jSession);
        delegate.loadAll(PersistentCategory.class, Collections.singletonList("abc"));
        ArgumentCaptor<GraphModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).isEqualTo("MATCH (n:`Category`:`Entity`) WHERE n.`uuid` IN $ids WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
    }

    @Test // GH-651
    public void shouldUseAllLabelsForOneEntityInInheritanceScenario() {

        LoadOneDelegate delegate = new LoadOneDelegate(neo4jSession);
        delegate.load(PersistentCategory.class, "abc");
        ArgumentCaptor<GraphModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).isEqualTo("MATCH (n:`Category`:`Entity`) WHERE n.`uuid` = $id WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
    }

    @Test // GH-651
    public void shouldUseAllLabelsForAllEntitiesInInheritanceScenario() {

        LoadByTypeDelegate delegate = new LoadByTypeDelegate(neo4jSession);
        delegate.loadAll(PersistentCategory.class);
        ArgumentCaptor<GraphModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).isEqualTo("MATCH (n:`Category`:`Entity`) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
    }

    @Test // GH-651
    public void shouldUseOnlyOneLabelForRelationshipEntities() {

        LoadByIdsDelegate delegate = new LoadByIdsDelegate(neo4jSession);
        delegate.loadAll(SomeRelationshipEntity.class, Arrays.asList(1L, 2L));
        ArgumentCaptor<GraphModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).startsWith("MATCH ()-[r0:`SOME_RELATIONSHIP`]-() WHERE ID(r0) IN $ids");
    }

    @Test // GH-651
    public void shouldUseOnlyOneLabelForOneRelationshipEntity() {

        LoadOneDelegate delegate = new LoadOneDelegate(neo4jSession);
        delegate.load(SomeRelationshipEntity.class, 1L);
        ArgumentCaptor<GraphModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).startsWith("MATCH ()-[r0:`SOME_RELATIONSHIP`]->() WHERE ID(r0)=$id");
    }

    @Test // GH-651
    public void shouldUseOnlyOneLabelForAllRelationshipEntities() {

        LoadByTypeDelegate delegate = new LoadByTypeDelegate(neo4jSession);
        delegate.loadAll(SomeRelationshipEntity.class);
        ArgumentCaptor<GraphRowListModelRequest> argumentCaptor = ArgumentCaptor.forClass(GraphRowListModelRequest.class);

        verify(request).execute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getStatement()).startsWith("MATCH ()-[r0:`SOME_RELATIONSHIP`]-()");
    }
}
