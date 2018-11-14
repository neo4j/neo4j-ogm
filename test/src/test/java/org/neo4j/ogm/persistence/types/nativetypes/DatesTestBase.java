package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class DatesTestBase {

    protected static SessionFactory sessionFactory;

    @Test
    public void convertPersistAndLoadLocalDate() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        LocalDate now = LocalDate.now();
        sometime.setLocalDate(now);
        session.save(sometime);

        session.clear();
        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.getLocalDate()).isEqualTo(now);

    }

    @Test
    public void convertPersistAndLoadLocalDateTime() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        LocalDateTime now = LocalDateTime.now();
        sometime.setLocalDateTime(now);
        session.save(sometime);

        session.clear();
        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.getLocalDateTime()).isEqualTo(now);
    }

    static class Sometime {

        LocalDate localDate;
        LocalDateTime localDateTime;
        private Long id;

        LocalDate getLocalDate() {
            return localDate;
        }

        void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        LocalDateTime getLocalDateTime() {
            return localDateTime;
        }

        void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }
    }
}
