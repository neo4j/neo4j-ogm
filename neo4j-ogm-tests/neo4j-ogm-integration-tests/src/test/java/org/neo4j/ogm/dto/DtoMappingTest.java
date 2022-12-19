package org.neo4j.ogm.dto;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.domain.dto.SomeDto;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Gerrit Meier
 */
public class DtoMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void setupConnectionAndDatabase() {
        Configuration ogmConfiguration = getBaseConfigurationBuilder()
            .build();

        sessionFactory = new SessionFactory(ogmConfiguration, "org.neo4j.ogm.domain.dto");
    }

    @Test
    public void shouldMapSingleDto() {

        Session session = sessionFactory.openSession();
        String dateValue = LocalDate.now().toString();
        List<SomeDto> dtos = session.queryDto("RETURN 'Hello' as valueA, 123 as valueB, $dateValue as valueC", Collections.singletonMap("dateValue", dateValue), SomeDto.class);

        assertThat(dtos).hasSize(1);
        SomeDto dto = dtos.get(0);
        assertThat(dto.valueA).isEqualTo("Hello");
        assertThat(dto.valueB).isEqualTo(123);
        assertThat(dto.valueC).isInstanceOf(LocalDate.class);
    }

    @Test
    public void shouldMapSingleDtoWithArrays() {

        Session session = sessionFactory.openSession();
        List<SomeDto> dtos = session.queryDto("RETURN ['a','b','c'] as valueD", Collections.emptyMap(), SomeDto.class);

        assertThat(dtos).hasSize(1);
        SomeDto dto = dtos.get(0);
        assertThat(dto.valueD).containsExactly("a", "b", "c");
    }

    @Test
    public void shouldMapCollectionOfDto() {
        Session preparingSession = sessionFactory.openSession();

        String dateValue = LocalDate.now().toString();

        preparingSession.query("MATCH (n) detach delete n", Collections.emptyMap()).queryStatistics();
        preparingSession.query("CREATE (m:Object{value1:'Hello', value2:123, value3:$dateValue})", Collections.singletonMap("dateValue", dateValue)).queryStatistics();
        preparingSession.query("CREATE (m:Object{value1:'Hello2', value2:1234, value3:$dateValue})", Collections.singletonMap("dateValue", dateValue)).queryStatistics();
        preparingSession.clear();

        Session session = sessionFactory.openSession();
        Iterable<SomeDto> dtos = session.queryDto("MATCH (o:Object) return o.value1 as valueA, o.value2 as valueB, o.value3 as valueC", Collections.emptyMap(), SomeDto.class);

        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting("valueA").containsExactlyInAnyOrder("Hello", "Hello2");
        assertThat(dtos).extracting("valueB").containsExactlyInAnyOrder(123, 1234);
        assertThat(dtos).extracting("valueC").allSatisfy(o -> {
            assertThat(o).isNotNull();
            assertThat(o).isInstanceOf(LocalDate.class);
        });
    }
}
