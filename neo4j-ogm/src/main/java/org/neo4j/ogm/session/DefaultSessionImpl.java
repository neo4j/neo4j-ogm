package org.neo4j.ogm.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.neo4j.graphmodel.GraphModel;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.cypher.statement.ParameterisedStatements;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.mapper.GraphObjectMapper;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.ObjectCypherMapper;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.session.request.DefaultRequestHandler;
import org.neo4j.ogm.session.request.Neo4jRequestHandler;
import org.neo4j.ogm.session.request.TransactionRequestHandler;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;
import org.neo4j.ogm.session.request.strategy.VariableDepthQuery;
import org.neo4j.ogm.session.response.GraphModelResponseHandler;
import org.neo4j.ogm.session.response.Neo4jResponseHandler;
import org.neo4j.ogm.session.response.RowModelResponseHandler;
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.transaction.Transaction;

import java.lang.reflect.Field;
import java.util.*;

public class DefaultSessionImpl implements Session {

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final ObjectMapper mapper;
    private final String autoCommitUrl;

    private Neo4jRequestHandler<String> requestHandler;
    private TransactionRequestHandler transactionRequestHandler;
    private Transaction transaction;

    private long buildTime;
    private long jsonTime;
    private long executeTime;
    private long processTime;

    public DefaultSessionImpl(MetaData metaData, String url, CloseableHttpClient client, ObjectMapper mapper) {

        this.metaData = metaData;
        this.mappingContext = new MappingContext();
        this.mapper = mapper;
        this.requestHandler = new DefaultRequestHandler(client);
        this.transactionRequestHandler = new TransactionRequestHandler(client, url);
        this.autoCommitUrl = autoCommit(url);
    }

    public void setRequestHandler(Neo4jRequestHandler<String> requestHandler) {
        this.requestHandler = requestHandler;
    }

    private Transaction getOrCreateTransaction() {
        if (this.transaction != null) {
            return this.transaction;
        } else {
            return new Transaction(mappingContext, autoCommitUrl);
        }
    }

    private Neo4jResponseHandler<GraphModel> executeGraphModelQuery(GraphModelQuery query) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        Transaction tx = getOrCreateTransaction();
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            Neo4jResponseHandler<String> responseHandler = requestHandler.execute(tx.url(), json);
            return new GraphModelResponseHandler(responseHandler, mapper);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    private Neo4jResponseHandler<RowModel> executeRowModelQuery(RowModelQuery query) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        Transaction tx = getOrCreateTransaction();
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            Neo4jResponseHandler<String> responseHandler = requestHandler.execute(tx.url(), json);
            return new RowModelResponseHandler(responseHandler, mapper);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    private Neo4jResponseHandler<String> executeStatement(ParameterisedStatement statement) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(statement);
        Transaction tx = getOrCreateTransaction();
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            return requestHandler.execute(tx.url(), json);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    @Override
    public <T> T load(Class<T> type, Long id) {
        return load(type, id, 1);
    }

    @Override
    public <T> T load(Class<T> type, Long id, int depth) {
        return loadOne(type, executeGraphModelQuery(new VariableDepthQuery().findOne(id, depth)));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, ids, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth) {
        return loadAll(type, executeGraphModelQuery(new VariableDepthQuery().findAll(ids, depth)));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        return loadAll(type, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, int depth) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        return loadAll(type, executeGraphModelQuery(new VariableDepthQuery().findByLabel(classInfo.label(), depth)));
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
    public <T> void deleteAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        executeStatement(new DeleteStatements().deleteByLabel(classInfo.label())).close();

    }

    @Override
    public Transaction beginTransaction() {
        // todo: ensure existing transaction is not pending
        // todo: make this threadsafe
        if (transaction != null && transaction.status() == (Transaction.OPEN | Transaction.PENDING)) {
            throw new RuntimeException("The current transaction should be rolled back or committed before beginning a new one");
        }
        transaction = new Transaction(mappingContext, transactionRequestHandler.execute());
        return transaction;
    }

    @Override
    public void close() {

    }

    @Override
    public void execute(String statement) {
        ParameterisedStatement parameterisedStatement = new ParameterisedStatement(statement, Utils.map());
        executeStatement(parameterisedStatement).close();
    }

    @Override
    public void purge() {
        executeStatement(new DeleteStatements().purge()).close();
    }

    @Override
    public <T> void save(T object) {
        save(object, -1); // default : full tree of changed objects
    }

    @Override
    public <T> void save(T object, int depth) {

        // should we allow mutable operations outside of a user-defined transaction?
        Transaction tx = getOrCreateTransaction();

        long now = System.currentTimeMillis();

        CypherContext context = new ObjectCypherMapper(metaData, new MappingContext()).mapToCypher(object, depth);

        buildTime += (System.currentTimeMillis() - now);
        try {
            List<ParameterisedStatement> statements = context.getStatements();
            now = System.currentTimeMillis();
            String json = mapper.writeValueAsString(new ParameterisedStatements(statements));
            //System.out.println(json);
            jsonTime += (System.currentTimeMillis() - now);
            now = System.currentTimeMillis();
            RowModelResponseHandler responseHandler = new RowModelResponseHandler(requestHandler.execute(tx.url(), json), mapper);
            executeTime += (System.currentTimeMillis() - now);
            now = System.currentTimeMillis();

            String[] variables = responseHandler.columns();
            RowModel rowModel;
            while ((rowModel = responseHandler.next()) != null) {
                Object[] results = rowModel.getValues();
                for (int i = 0; i < variables.length; i++) {
                    String variable = variables[i];
                    //System.out.println(variable);
                    Object persisted = context.getNewObject(variable);
                    Long identity = Long.parseLong(results[i].toString());
                    // todo: metaData should cache this stuff
                    ClassInfo classInfo = metaData.classInfo(persisted.getClass().getName());
                    Field identityField = classInfo.getField(classInfo.identityField());
                    FieldAccess.write(identityField, persisted, identity);
                }
            }
            processTime += (System.currentTimeMillis() - now);
            responseHandler.close();

            // todo: tx.append(context.log)
            // todo: (if not on tx endpoint) tx.commit();
        } catch (Exception e) {
            // todo: tx.rollback // just clear the commit log
            throw new RuntimeException(e);
        }

    }

    @Override
    public <T> void delete(T object) {
        ClassInfo classInfo = metaData.classInfo(object.getClass().getName());
        Field identityField = classInfo.getField(classInfo.identityField());
        Long identity = (Long) FieldAccess.read(identityField, object);
        if (identity != null) {
            executeStatement(new DeleteStatements().delete(identity)).close();
        }
    }

    private <T> T loadOne(Class<T> type, Neo4jResponseHandler<GraphModel> stream) {
        GraphModel graphModel = stream.next();
        if (graphModel != null) {
            GraphObjectMapper ogm = new GraphObjectMapper(metaData, mappingContext);
            stream.close();
            return ogm.load(type, graphModel);
        }
        return null;
    }

    private <T> Collection<T> loadAll(Class<T> type, Neo4jResponseHandler<GraphModel> stream) {
        Set<T> objects = new HashSet<>();
        GraphObjectMapper ogm = new GraphObjectMapper(metaData, mappingContext);
        GraphModel graphModel;
        while ((graphModel = stream.next()) != null) {
            objects.addAll(ogm.loadAll(type, graphModel));
        }
        stream.close();
        return objects;
    }

    private String autoCommit(String url) {
        if (url == null) {
            return url;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }

        return url + "db/data/transaction/commit";
    }

    public long getBuildTime() {
        return buildTime;
    }

    public long getJsonTime() {
        return jsonTime;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public long getProcessTime() {
        return processTime;
    }
}
