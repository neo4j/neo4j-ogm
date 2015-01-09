package org.neo4j.spring.integration.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.spring.integration.web.context.PersistenceContext;
import org.neo4j.spring.integration.web.context.WebAppContext;
import org.neo4j.spring.integration.web.domain.User;
import org.neo4j.spring.integration.web.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {WebAppContext.class, PersistenceContext.class})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WebIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        User adam = new User("Adam");
        User daniela = new User("Daniela");
        User michal = new User("Michal");
        User vince = new User("Vince");

        adam.befriend(daniela);
        daniela.befriend(michal);
        michal.befriend(vince);

        userRepository.save(adam);
    }

    @Test
    public void shouldNotShareSessionBetweenRequests() throws Exception {
        mockMvc.perform(get("/user/{name}/friends", "Adam"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Daniela"));

        mockMvc.perform(get("/user/{name}/friends", "Vince"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Michal"));
    }
}
