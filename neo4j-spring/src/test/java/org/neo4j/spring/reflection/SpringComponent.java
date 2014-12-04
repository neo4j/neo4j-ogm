package org.neo4j.spring.reflection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SpringComponent {

    @Autowired
    @Qualifier("concretelyWiredRef")
    Ref<String> concretelyWiredRef;

    @Autowired
    @Qualifier("abstractlyWiredRef")
    Ref<String> abstractlyWiredRef;

    @Autowired
    @Qualifier("interfaceWiredRef")
    Ref<String> interfaceWiredRef;



}
