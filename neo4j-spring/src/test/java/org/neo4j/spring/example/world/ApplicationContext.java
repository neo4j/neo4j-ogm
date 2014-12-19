package org.neo4j.spring.example.world;

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
@ComponentScan({"org.neo4j.spring.example.world"})
@PropertySource("classpath:application.properties")
@EnableNeo4jRepositories("org.neo4j.spring.example.world")
@EnableTransactionManagement
class ApplicationContext extends Neo4jConfiguration {

    @Bean
    public Neo4jServer neo4jServer() {
        return new InProcessServer();
        //production: return new OutOfProcessServer(environment.getRequiredProperty("url");
    }
}