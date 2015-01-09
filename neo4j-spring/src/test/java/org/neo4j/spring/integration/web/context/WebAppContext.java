package org.neo4j.spring.integration.web.context;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ComponentScan({"org.neo4j.spring.integration.web.controller"})
@EnableWebMvc
public class WebAppContext extends WebMvcConfigurerAdapter {
}
