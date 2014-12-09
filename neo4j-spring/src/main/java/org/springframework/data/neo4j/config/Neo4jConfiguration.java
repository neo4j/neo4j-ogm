package org.springframework.data.neo4j.config;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.neo4j.server.Neo4jServer;

import javax.annotation.Resource;

@Configuration
public abstract class Neo4jConfiguration {

    @Resource
    private Environment environment;

    @Bean
    SessionFactory getSessionFactory() {
        return new SessionFactory(environment.getRequiredProperty("domain"));
    }

    @Bean
    Session getSession() throws Exception {
        return getSessionFactory().openSession(neo4jServer().url());
    }

    @Bean
    public PersistenceExceptionTranslator persistenceExceptionTranslator() {
        return new PersistenceExceptionTranslator() {
            @Override
            public DataAccessException translateExceptionIfPossible(RuntimeException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public abstract Neo4jServer neo4jServer();


}
