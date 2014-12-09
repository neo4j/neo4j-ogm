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

public class GraphRepositoryFactory<S, T> extends RepositoryFactorySupport {

    private final Session session;

    public GraphRepositoryFactory(Session session) {
        System.out.println("creating GraphRepositoryFactory bean");
        this.session = session;
    }

    @Override
    public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> tClass) {
        System.out.println("getting entity information for class: " + tClass);
        return (EntityInformation<T, ID>) new GraphEntityInformation<>(tClass);
    }

    @Override
    protected Object getTargetRepository(RepositoryMetadata repositoryMetadata) {
        System.out.println("getting the repository implementation for domain type: " + repositoryMetadata.getDomainType());
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

//        for (RepositoryProxyPostProcessor processor : postProcessors) {
//            processor.postProcess(result);
//        }
//
//        result.addAdvice(new QueryExecutorMethodInterceptor(information, customImplementation, target));

        return (T) result.getProxy(classLoader);
    }

    class GraphEntityInformation<T> extends AbstractEntityInformation<T, Long> {

        public GraphEntityInformation(Class<T> clazz) {
            super(clazz);
        }
        @Override
        public Long getId(T t) {
            throw new RuntimeException("why?");
        }

        @Override
        public Class<Long> getIdType() {
            return Long.class;
        }
    }

//    @Override
//    protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key) {
//        System.out.println("getting the query lookup strategy object");
//        return new QueryLookupStrategy() {
//            @Override
//            public RepositoryQuery resolveQuery(final Method method, final RepositoryMetadata repositoryMetadata, NamedQueries namedQueries) {
//
//                System.out.println("resolving query for method: " + method);
//
//                return new RepositoryQuery() {
//                    @Override
//                    public Object execute(Object[] parameters) {
//                        System.out.println("executing query with parameters: " + parameters);
//                        return new Object();
//                    }
//
//                    @Override
//                    public QueryMethod getQueryMethod() {
//                        System.out.println("getting query method " + method);
//                        return new QueryMethod(method, repositoryMetadata);
//                    }
//                };
//            }
//        };
//    }
}
