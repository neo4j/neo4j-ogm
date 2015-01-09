package org.neo4j.spring.integration.helloworld.context;

import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.spring.InProcessServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.neo4j.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({"org.neo4j.spring.integration.helloworld.*"})
@PropertySource("classpath:helloworld.properties")
@EnableNeo4jRepositories("org.neo4j.spring.integration.helloworld.repo")
@EnableTransactionManagement
public class HelloWorldContext extends Neo4jConfiguration {

    @Bean
    @Override
    public Neo4jServer neo4jServer() {
        return new InProcessServer();
        //production: return new RemoteServer(environment.getRequiredProperty("url");
    }

    @Bean
    @Override
    public SessionFactory getSessionFactory() {
        return new SessionFactory("org.neo4j.spring.integration.helloworld.domain");
    }

}