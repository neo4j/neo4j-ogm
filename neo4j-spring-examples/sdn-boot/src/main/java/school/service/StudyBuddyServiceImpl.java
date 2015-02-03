package school.service;

import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Service;
import school.domain.StudyBuddy;
import school.repository.StudyBuddyRepository;

import java.util.HashMap;
import java.util.Map;

@Service("studyBuddyService")
public class StudyBuddyServiceImpl extends GenericService<StudyBuddy> implements StudyBuddyService {

    private static final String MOST_POPULAR_STUDY_BUDDIES="MATCH(s:StudyBuddy)<-[:BUDDY]-(p:Student) return p, count(s) as buddies ORDER BY buddies DESC";

    @Autowired
    private StudyBuddyRepository repository;

    @Autowired
    private Session session;

    @Override
    public GraphRepository<StudyBuddy> getRepository() {
        return repository;
    }

    @Override
    public Iterable<Map<String,Object>> getStudyBuddiesByPopularity() {
        return session.query(MOST_POPULAR_STUDY_BUDDIES, new HashMap<String, Object>());
    }
}
