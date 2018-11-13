/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session;

import static java.util.Objects.*;
import static org.neo4j.ogm.config.AutoIndexMode.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.neo4j.ogm.autoindex.AutoIndexManager;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.exception.core.ConfigurationException;
import org.neo4j.ogm.id.IdStrategy;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.reflect.ReflectionEntityInstantiator;
import org.neo4j.ogm.session.event.EventListener;

/**
 * This is the main initialization point of OGM. Used to create {@link Session} instances for interacting with Neo4j.
 * In a typical scenario one instance of SessionFactory is created, shared across whole application.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class SessionFactory {

    private final MetaData metaData;
    private final Driver driver;
    private final List<EventListener> eventListeners;

    private LoadStrategy loadStrategy = LoadStrategy.SCHEMA_LOAD_STRATEGY;
    private EntityInstantiator entityInstantiator;

    /**
     * Constructs a new {@link SessionFactory} by initialising the object-graph mapping meta-data from the given list of domain
     * object packages and starts up the Neo4j database in embedded mode.  If the embedded driver is not available this method
     * will throw a <code>Exception</code>.
     * <p>
     * The package names passed to this constructor should not contain wildcards or trailing full stops, for example,
     * "org.springframework.data.neo4j.example.domain" would be fine.  The default behaviour is for sub-packages to be scanned
     * and you can also specify fully-qualified class names if you want to cherry pick particular classes.
     * </p>
     * Indexes will also be checked or built if configured.
     *
     * @param packages The packages to scan for domain objects
     */
    public SessionFactory(String... packages) {
        this(new Configuration.Builder().build(), packages);
    }

    /**
     * Constructs a new {@link SessionFactory} by initialising the object-graph mapping meta-data from the given list of domain
     * object packages, and also sets the baseConfiguration to be used.
     * <p>
     * The package names passed to this constructor should not contain wildcards or trailing full stops, for example,
     * "org.springframework.data.neo4j.example.domain" would be fine.  The default behaviour is for sub-packages to be scanned
     * and you can also specify fully-qualified class names if you want to cherry pick particular classes.
     * </p>
     * Indexes will also be checked or built unless auto index mode is set to <code>NONE</code>.
     *
     * @param configuration The baseConfiguration to use
     * @param packages      The packages to scan for domain objects
     */
    public SessionFactory(Configuration configuration, String... packages) {
        this(newConfiguredDriverInstance(configuration), packages);

        if (configuration.getAutoIndex() != NONE) {
            runAutoIndexManager(configuration);
        }
    }

    /**
     * Create a session factory with given driver
     * Use this constructor when you need to provide fully customized driver.
     * Indexes will not be automatically created.
     *
     * @param driver   driver to be used with this SessionFactory
     * @param packages The packages to scan for domain objects
     */
    public SessionFactory(Driver driver, String... packages) {
        this.metaData = new MetaData(driver.getTypeSystem(), packages);
        this.driver = driver;
        this.eventListeners = new CopyOnWriteArrayList<>();
        this.entityInstantiator = new ReflectionEntityInstantiator(metaData);
    }

    /**
     * Opens a session and runs the auto-index manager with the given configuration and the metadata configured in this
     * factory. This method can be run multiple times.
     *
     * @param configuration only used to configure aspects of the auto-index manager, not for the session factory at this point.
     */
    public final void runAutoIndexManager(Configuration configuration) {

        Neo4jSession neo4jSession = (Neo4jSession) openSession();

        AutoIndexManager autoIndexManager = new AutoIndexManager(this.metaData, configuration, neo4jSession);
        autoIndexManager.run();
    }

    /**
     * Retrieves the meta-data that was built up when this {@link SessionFactory} was constructed.
     *
     * @return The underlying {@link MetaData}
     */
    public MetaData metaData() {
        return metaData;
    }

    /**
     * Opens a new Neo4j mapping {@link Session} using the Driver specified in the OGM baseConfiguration
     * The driver should be configured to connect to the database using the appropriate
     * DriverConfig
     *
     * @return A new {@link Session}
     */
    public Session openSession() {
        return new Neo4jSession(metaData, driver, eventListeners, loadStrategy, entityInstantiator);
    }

    /**
     * Registers the specified listener on all <code>Session</code> events generated from
     * <code>this SessionFactory</code>.
     * Only Session instances created after this call are affected.
     *
     * @param eventListener The event listener to register.
     */
    public void register(EventListener eventListener) {
        eventListeners.add(eventListener);
    }

    /**
     * Removes the the specified listener from <code>this SessionFactory</code>.
     * Only Session instances created after this call are affected.
     *
     * @param eventListener The event listener to deregister.
     */
    public void deregister(EventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    /**
     * Returns current load strategy
     *
     * @return load strategy
     */
    public LoadStrategy getLoadStrategy() {
        return loadStrategy;
    }

    /**
     * Sets the LoadStrategy
     * Will be used for all queries on subsequently created sessions. This also can be set on individual Session
     * instances.
     *
     * @param loadStrategy load strategy
     */
    public void setLoadStrategy(LoadStrategy loadStrategy) {
        this.loadStrategy = loadStrategy;
    }

    public void setEntityInstantiator(EntityInstantiator entityInstantiator) {
        this.entityInstantiator = entityInstantiator;
    }

    /**
     * Returns driver used by this SessionFactory
     *
     * @return driver
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Closes this session factory
     * Also closes any underlying resources, like driver etc.
     */
    public void close() {
        driver.close();
    }

    /**
     * Register an instance of {@link IdStrategy}
     * This instance will be used for generation of ids annotated with
     * {@code @Id @GeneratedValue(strategy=SomeClass.class)}
     *
     * @param strategy {@link org.neo4j.ogm.id.IdStrategy} to use
     */
    public void register(IdStrategy strategy) {
        requireNonNull(strategy);
        for (ClassInfo classInfo : metaData.persistentEntities()) {
            if (strategy.getClass().equals(classInfo.idStrategyClass())) {
                classInfo.registerIdGenerationStrategy(strategy);
            }
        }
    }

    private static Driver newConfiguredDriverInstance(Configuration configuration) {

        String driverClassName = configuration.getDriverClassName();
        try {
            final Class<?> driverClass = Class.forName(driverClassName);
            final Driver driver = (Driver) driverClass.newInstance();
            driver.configure(configuration);
            return driver;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new ConfigurationException("Could not load driver class " + driverClassName, e);
        }
    }

}
