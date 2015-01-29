package school.service;

import org.springframework.data.neo4j.repository.GraphRepository;
import school.domain.Entity;

public abstract class GenericService<T> implements Service<T> {

    @Override
    public Iterable<T> findAll() {
        return getRepository().findAll(0);
    }

    @Override
    public T find(Long id) {
        return getRepository().findOne(id);
    }

    @Override
    public void delete(Long id) {
        getRepository().delete(id);
    }

    @Override
    public T createOrUpdate(T entity) {
        getRepository().save(entity);
        return find(((Entity) entity).getId());
    }

    public abstract GraphRepository<T> getRepository();
}
