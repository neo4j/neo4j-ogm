package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.TransientRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SimpleTransaction implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);
    private final MappingContext mappingContext;
    private final String url;
    private final boolean autocommit;

    private final List<CypherContext> contexts;

    private Status status = Status.OPEN;

    public SimpleTransaction(MappingContext mappingContext, String url) {
        this.mappingContext = mappingContext;
        this.url = url;
        this.autocommit = url.endsWith("/commit");
        this.contexts = new ArrayList<>();
    }

    public final void append(CypherContext context) {
        logger.debug("Appending transaction context " + context);
        if (status == Status.OPEN || status == Status.PENDING) {
            contexts.add(context);
            status = Status.PENDING;
            if (autocommit) {
                commit();
            }
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot accept new operations");
        }
    }

    public final String url() {
        return url;
    }

    public void rollback() {
        logger.info("rollback invoked");
        if (status == Status.OPEN || status == Status.PENDING) {
            contexts.clear();
            status = Status.ROLLEDBACK;
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot rollback");
        }
    }

    public void commit() {
        logger.info("commit invoked");
        if (status == Status.OPEN || status == Status.PENDING) {
            for (CypherContext cypherContext : contexts) {
                logger.debug("Synchronizing transaction context " + cypherContext + " with session context");
                // todo : subclass these also : is this really necessary?
                for (Object o : cypherContext.log())  {
                    logger.debug("checking cypher context object: " + o);
                    if (o instanceof MappedRelationship) {
                        mappingContext.remember((MappedRelationship) o);
                    } else if (!(o instanceof TransientRelationship)) {
                        mappingContext.remember(o);
                    }
                }
                logger.debug("checked objects: " + cypherContext.log().size());
            }
//            logger.info("relationships registered active:");
//            for (MappedRelationship mappedRelationship : mappingContext.mappedRelationships()) {
//                logger.info("(${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
//            }
            contexts.clear();
            status = Status.COMMITTED;
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot commit");
        }
    }

    public final Status status() {
        return status;
    }

    public void close() {
        status = Status.CLOSED;
    }
}