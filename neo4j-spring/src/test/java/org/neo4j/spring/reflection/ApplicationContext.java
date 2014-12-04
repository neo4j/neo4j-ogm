package org.neo4j.spring.reflection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.neo4j.spring.reflection")
class ApplicationContext {

    @Bean
    Ref<String> abstractlyWiredRef() {
        return new TypeRef<String>() {};
    }

    @Bean
    Ref<String> concretelyWiredRef() {
        return new TypeRef<>();
    }

    @Bean
    Ref<String> interfaceWiredRef() {
        return new Ref<String>() {

            @Override
            public void set(String s) {

            }

            @Override
            public String get() {
                return null;
            }
        };
    }


//    @Resource
//    private Environment environment;
//
//    @Bean
//    SessionFactory getSessionFactory() {
//        return new SessionFactory(environment.getRequiredProperty("domain"));
//    }
//
//    @Bean
//    Session getSession() throws Exception {
//        // do this in production.
//        //return getSessionFactory().openSession(environment.getRequiredProperty("url"));
//
//        return getSessionFactory().openSession(getNeo4jServer().url());
//    }
//
//    @Bean Neo4jServer getNeo4jServer() throws Exception {
//        return new Neo4jServer();
//    }

}