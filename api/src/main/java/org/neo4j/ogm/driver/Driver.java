/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.driver;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.TypeSystem.NoNativeTypes;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public interface Driver extends AutoCloseable {

    void configure(Configuration config);

    /**
     * @return A factory method for creating suppliers of transactions for a given transaction manager
     */
    Function<TransactionManager, BiFunction<Transaction.Type, Iterable<String>, Transaction>> getTransactionFactorySupplier();

    void close();

    /**
     * @param transaction Current transaction, may be null, depending on the driver
     * @return A new request handler
     */
    Request request(Transaction transaction);

    Configuration getConfiguration();

    default Function<String, String> getCypherModification() {
        return Function.identity();
    }

    /**
     * Indicates if the driver requires an explicit transaction to run queries.
     * Used to know if transactions has to be explicitly triggered by OGM before accessing DB.
     *
     * @return true if client code has to setup a transaction, false if the driver can auto-commit.
     */
    default boolean requiresTransaction() {
        return true;
    }

    /**
     * This returns the type system of the specific drivers. A type system for a driver is set of types that
     * are special to Neo4j and can be represented in either Java built-ins or dedicated types. The driver interface
     * defaults to a type system that does not support any native types.
     *
     * @return This driver's type system.
     */
    default TypeSystem getTypeSystem() {
        return NoNativeTypes.INSTANCE;
    }

    /**
     * An {@link ExceptionTranslator} translating driver specific exception into a Neo4j-OGM api exceptions.
     *
     * @return The default translator returning the exception itself.
     * @since 3.2
     */
    default ExceptionTranslator getExceptionTranslator() {
        return e -> e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
    }

    /**
     * Unwraps this Neo4j-OGM specific driver into it's underlying physical driver of type {@code T} if the concrete driver's
     * transport is compatible with the speficied class.
     *
     * @param clazz The class into which the driver should be unwrapped
     * @param <T>   Type of the class
     * @return The unwrapped driver
     * @throws IllegalArgumentException if this driver cannot be unwrapped into the given class.
     */
    default <T> T unwrap(Class<T> clazz) {

        String message = String.format("Cannot unwrap '%s' into '%s'", this.getClass().getName(), clazz.getName());
        throw new IllegalArgumentException(message);
    }
}
