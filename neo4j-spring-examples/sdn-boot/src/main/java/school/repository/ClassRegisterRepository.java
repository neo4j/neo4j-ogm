package school.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import school.domain.ClassRegister;

public interface ClassRegisterRepository extends GraphRepository<ClassRegister> {

}

