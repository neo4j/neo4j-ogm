package org.springframework.data.neo4j.repository;

import org.neo4j.ogm.session.Session;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.data.repository.core.support.AnnotationRepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

public class GraphRepositoryFactory extends RepositoryFactorySupport {

    private final Session session;

    public GraphRepositoryFactory(Session session) {
        this.session = session;
    }

    @Override
    public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> tClass) {
        return (EntityInformation<T, ID>) new GraphEntityInformation<>(tClass);
    }

    @Override
    protected Object getTargetRepository(RepositoryMetadata repositoryMetadata) {
        return new GraphRepositoryImpl<>(repositoryMetadata.getDomainType(), session);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata repositoryMetadata) {
        return Long.class;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public <T> T getRepository(Class<T> repositoryInterface, Object customImplementation) {

        ClassLoader classLoader = org.springframework.util.ClassUtils.getDefaultClassLoader();

        RepositoryMetadata metadata = Repository.class.isAssignableFrom(repositoryInterface)
                ? new DefaultRepositoryMetadata(repositoryInterface)
                : new AnnotationRepositoryMetadata(repositoryInterface);

        Class<?> customImplementationClass = null == customImplementation ? null : customImplementation.getClass();
        RepositoryInformation information = getRepositoryInformation(metadata, customImplementationClass);

        Object target = getTargetRepository(information);

        // Create proxy
        ProxyFactory result = new ProxyFactory();
        result.setTarget(target);
        result.setInterfaces(new Class[] { repositoryInterface, Repository.class });


        return (T) result.getProxy(classLoader);
    }

    class GraphEntityInformation<T> extends AbstractEntityInformation<T, Long> {
        public GraphEntityInformation(Class<T> clazz) {
            super(clazz);
        }

        @Override
        public Long getId(T t) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public Class<Long> getIdType() {
            return Long.class;
        }
    }
}
