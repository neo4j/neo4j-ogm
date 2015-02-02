package school.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;
import school.domain.StudyBuddy;

@Repository
public interface StudyBuddyRepository extends GraphRepository<StudyBuddy> {

}
