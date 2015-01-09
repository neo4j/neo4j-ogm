package org.neo4j.spring.integration.web.context;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.spring.InProcessServer;
import org.springframework.context.annotation.*;
import org.springframework.data.neo4j.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan({"org.neo4j.spring.integration.web"})
@EnableNeo4jRepositories("org.neo4j.spring.integration.web.repo")
@EnableTransactionManagement
public class PersistenceContext extends Neo4jConfiguration {

    @Bean
    @Override
    public Neo4jServer neo4jServer() {
        return new InProcessServer();
    }

    @Bean
    public SessionFactory getSessionFactory() {
        return new SessionFactory("org.neo4j.spring.integration.web.domain");
    }

    @Override
    @Bean
    @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Session getSession() throws Exception {
        return super.getSession();
    }
}
