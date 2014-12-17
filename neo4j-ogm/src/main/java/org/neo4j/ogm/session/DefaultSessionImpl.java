package org.neo4j.ogm.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.ObjectCypherMapper;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.session.request.*;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;
import org.neo4j.ogm.session.request.strategy.VariableDepthQuery;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.response.ResponseHandler;
import org.neo4j.ogm.session.response.SessionResponseHandler;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.UncommittedTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultSessionImpl implements Session {

    private final Logger logger = LoggerFactory.getLogger(DefaultSessionImpl.class);

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final ObjectMapper mapper;
    private final String autoCommitUrl;

    private RequestHandler requestHandler;
    private ResponseHandler responseHandler;

    private TransactionRequestHandler transactionRequestHandler;
    private Transaction transaction;

    public DefaultSessionImpl(MetaData metaData, String url, CloseableHttpClient client, ObjectMapper mapper) {
        this.metaData = metaData;
        this.mapper = mapper;
        this.mappingContext = new MappingContext(metaData);

        this.transactionRequestHandler = new TransactionRequestHandler(client, url);
        this.autoCommitUrl = autoCommit(url);

        this.requestHandler = new SessionRequestHandler(mapper, new DefaultRequest(client));
        this.responseHandler = new SessionResponseHandler(metaData, mappingContext);
    }

    public void setRequestHandler(Neo4jRequest<String> requestHandler) {
        this.requestHandler = new SessionRequestHandler(mapper, requestHandler);
    }

    @Override
    public <T> T load(Class<T> type, Long id) {
        return load(type, id, 0);
    }

    @Override
    public <T> T load(Class<T> type, Long id, int depth) {
        String url = getOrCreateTransaction().url();
        GraphModelQuery qry = new VariableDepthQuery().findOne(id, depth);
        try (Neo4jResponse<GraphModel> response = requestHandler.execute(qry, url)) {
            return responseHandler.loadById(type, response, id);
        }
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, ids, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth) {
        String url = getOrCreateTransaction().url();
        GraphModelQuery qry = new VariableDepthQuery().findAll(ids, depth);
        try (Neo4jResponse<GraphModel> response = requestHandler.execute(qry, url)) {
            return responseHandler.loadAll(type, response);
        }
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        return loadAll(type, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, int depth) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        String url = getOrCreateTransaction().url();
        GraphModelQuery qry = new VariableDepthQuery().findByLabel(classInfo.label(), depth);
        try (Neo4jResponse<GraphModel> response = requestHandler.execute(qry, url)) {
            return responseHandler.loadAll(type, response);
        }
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects) {
        return loadAll(objects, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, int depth) {

        if (objects == null || objects.isEmpty()) {
            return objects;
        }

        Set<Long> ids = new HashSet<>();
        Class type = objects.iterator().next().getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Field identityField = classInfo.getField(classInfo.identityField());
        for (Object o: objects) {
            ids.add((Long) FieldAccess.read(identityField, o));
        }
        return loadAll(type, ids, depth);
    }

    @Override
    public <T> Collection<T> loadByProperty(Class<T> type, Property<String, Object> property) {
        return loadByProperty(type, property, 1);
    }

    @Override
    public <T> Collection<T> loadByProperty(Class<T> type, Property<String, Object> property, int depth) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        String url = getOrCreateTransaction().url();
        GraphModelQuery qry = new VariableDepthQuery().findByProperty(classInfo.label(), property, depth);
        try (Neo4jResponse<GraphModel> response = requestHandler.execute(qry, url)) {
            return responseHandler.loadByProperty(type, response, property);
        }
    }

    @Override
    public Transaction beginTransaction() {
        if (transaction != null && transaction.status() == (Transaction.PENDING)) {
            throw new UncommittedTransactionException();
        }
        this.transaction = new Transaction(mappingContext, transactionRequestHandler.openTransaction());
        return transaction;
    }

    @Override
    public void close() {
        // what does this do??
    }

    @Override
    public void execute(String statement) {
        ParameterisedStatement parameterisedStatement = new ParameterisedStatement(statement, Utils.map());
        String url = getOrCreateTransaction().url();
        requestHandler.execute(parameterisedStatement, url).close();
    }

    @Override
    public void purge() {
        String url = getOrCreateTransaction().url();
        requestHandler.execute(new DeleteStatements().purge(), url).close();
        mappingContext.clear();
    }

    @Override
    public <T> void save(T object) {
        save(object, -1); // default : full tree of changed objects
    }

    @Override
    public <T> void save(T object, int depth) {
        Transaction tx = getOrCreateTransaction();
        CypherContext context = new ObjectCypherMapper(metaData, mappingContext).mapToCypher(object, depth);
        try (Neo4jResponse<String> response = requestHandler.execute(context.getStatements(), tx.url())) {
            responseHandler.updateObjects(context, response, mapper);
            tx.append(context);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void delete(T object) {

        ClassInfo classInfo = metaData.classInfo(object.getClass().getName());
        Field identityField = classInfo.getField(classInfo.identityField());
        Long identity = (Long) FieldAccess.read(identityField, object);
        if (identity != null) {
            String url = getOrCreateTransaction().url();
            ParameterisedStatement request = new DeleteStatements().delete(identity);
            try (Neo4jResponse<String> response = requestHandler.execute(request, url)) {
                // nothing to process on the response - looks a bit odd.
                // should be done on commit?? when do these objects disappear?
                mappingContext.getAll(object.getClass()).remove(object);
                // should also remove relationships associated with this object;

            }
        }
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        String url = getOrCreateTransaction().url();
        ParameterisedStatement request = new DeleteStatements().deleteByLabel(classInfo.label());
        try (Neo4jResponse<String> response = requestHandler.execute(request, url)) {
            // should be done on commit.
            mappingContext.getAll(type).clear();
            mappingContext.mappedRelationships().clear(); // not the real deal
        }
    }

    private static String autoCommit(String url) {
        if (url == null) return url;
        if (!url.endsWith("/")) url = url + "/";
        return url + "db/data/transaction/commit";
    }

    // if there is no user transaction, create a transient auto-commit one;
    private Transaction getOrCreateTransaction() {
        if (this.transaction != null) {
            return this.transaction;
        } else {
            return new Transaction(mappingContext, autoCommitUrl);
        }
    }
}