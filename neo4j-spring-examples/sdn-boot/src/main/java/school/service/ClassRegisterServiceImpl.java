package school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Service;
import school.domain.Course;
import school.repository.ClassRegisterRepository;

@Service("classRegisterService")
public class ClassRegisterServiceImpl extends GenericService<Course> implements ClassRegisterService {

    @Autowired
    private ClassRegisterRepository repository;

    @Override
    public GraphRepository<Course> getRepository() {
        return repository;
    }
}
