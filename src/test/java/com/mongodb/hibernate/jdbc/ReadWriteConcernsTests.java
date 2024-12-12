/*
 * Copyright 2024-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.hibernate.jdbc;

import static com.mongodb.hibernate.internal.MongoAssertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.hibernate.MongoSession;
import com.mongodb.hibernate.MongoSessionFactory;
import com.mongodb.hibernate.service.MongoClientCustomizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.bson.BsonDocument;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.spi.ServiceException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ReadWriteConcernsTests {

    private static final String EXAMPLE_UPDATE_MQL =
            """
            {
                insert: "books",
                documents: [
                    {
                        title: "War and Peace",
                        author: "Leo Tolstoy",
                        outOfStock: false
                    }
                ]
            }""";

    private MongoSessionFactory mongoSessionFactory;

    private final List<BsonDocument> commandsStarted = new ArrayList<>(3);

    @BeforeEach
    void setUp() {
        commandsStarted.clear();

        var commandListener = new CommandListener() {
            @Override
            public void commandStarted(CommandStartedEvent event) {
                commandsStarted.add(event.getCommand().clone());
            }
        };
        mongoSessionFactory = buildSessionFactory((builder, connectionString) -> {
            builder.addCommandListener(commandListener);
            builder.applyConnectionString(assertNotNull(connectionString));
        });
        mongoSessionFactory.inSession(
                session -> session.doWork(
                        conn -> conn.createStatement()
                                .executeUpdate(
                                        """
                    {
                        delete: "books",
                        deletes: [
                            { q: {}, limit: 0 }
                        ]
                    }""")));
    }

    @Nested
    class SessionFactoryLevelTests {

        @ParameterizedTest
        @MethodSource("getSessionFactoryConcerns")
        @DisplayName(
                "SessionFactory level non-null concerns will take effect when no session or transaction level concerns exist")
        void testSessionFactoryConcernst(@Nullable ReadConcern readConcern, @Nullable WriteConcern writeConcern) {
            // given
            mongoSessionFactory.setReadWriteConcerns(readConcern, writeConcern);

            // when
            mongoSessionFactory.inTransaction(session -> session.doWork(conn -> {
                var stmt = conn.createStatement();
                stmt.executeUpdate(EXAMPLE_UPDATE_MQL);
            }));

            // then
            assertCommandsStartedWithExpectedConcerns(readConcern, writeConcern);
        }

        private static Stream<Arguments> getSessionFactoryConcerns() {
            List<Arguments> arguments = new ArrayList<>();
            @Nullable ReadConcern[] readConcerns = {null, ReadConcern.LOCAL, ReadConcern.MAJORITY, ReadConcern.SNAPSHOT};
            for (ReadConcern readConcern : readConcerns) {
                arguments.add(Arguments.of(readConcern, null));
                arguments.add(Arguments.of(
                        readConcern,
                        WriteConcern.ACKNOWLEDGED
                                .withW(1)
                                .withWTimeout(33, TimeUnit.MILLISECONDS)
                                .withJournal(true)));
            }
            return arguments.stream();
        }
    }

    @Nested
    class SessionLevelTests {

        @ParameterizedTest
        @MethodSource("com.mongodb.hibernate.jdbc.ReadWriteConcernsTests#getNonNullConcerns")
        @DisplayName("Session level non-null concerns will take effect when no transaction level concerns exist")
        void testSessionLevelNonNullConcerns(ReadConcern readConcern, WriteConcern writeConcern) {
            // when
            mongoSessionFactory.inSession(readConcern, writeConcern, session -> {
                var mongoSession = session.unwrap(MongoSession.class);
                var transaction = mongoSession.beginTransaction();
                session.doWork(conn -> conn.createStatement().executeUpdate(EXAMPLE_UPDATE_MQL));
                transaction.commit();
            });

            // then
            assertCommandsStartedWithExpectedConcerns(readConcern, writeConcern);
        }

        @ParameterizedTest
        @CsvSource({"true, false", "false, true", "true, true"})
        @DisplayName("Session level null concerns will inherit SessionFactory level non-null counterpart")
        void testSessionLevelNullConcerns(boolean readConcernIsNull, boolean writeConcernIsNull) {
            // given
            var sessionFactoryReadConcern = ReadConcern.MAJORITY;
            var sessionFactoryWriteConcern = WriteConcern.ACKNOWLEDGED.withW(1).withJournal(true);
            mongoSessionFactory.setReadWriteConcerns(sessionFactoryReadConcern, sessionFactoryWriteConcern);

            var sessionNonNullReadConcern = ReadConcern.SNAPSHOT;
            var sessionNonNullWriteConcern = WriteConcern.ACKNOWLEDGED.withW(1).withJournal(false);

            // when
            mongoSessionFactory.inSession(
                    readConcernIsNull ? null : sessionNonNullReadConcern,
                    writeConcernIsNull ? null : sessionNonNullWriteConcern,
                    session -> {
                        var mongoSession = session.unwrap(MongoSession.class);
                        var transaction = mongoSession.beginTransaction();
                        session.doWork(conn -> conn.createStatement().executeUpdate(EXAMPLE_UPDATE_MQL));
                        transaction.commit();
                    });

            //  then
            assertCommandsStartedWithExpectedConcerns(
                    readConcernIsNull ? sessionFactoryReadConcern : sessionNonNullReadConcern,
                    writeConcernIsNull ? sessionFactoryWriteConcern : sessionNonNullWriteConcern);
        }
    }

    @Nested
    class TransactionLevelTests {

        @ParameterizedTest
        @MethodSource("com.mongodb.hibernate.jdbc.ReadWriteConcernsTests#getNonNullConcerns")
        @DisplayName("Transaction level non-null concerns will take effect")
        void testTransactionLevelNonNullConcerns(ReadConcern readConcern, WriteConcern writeConcern) {
            // when
            mongoSessionFactory.inTransaction(
                    readConcern,
                    writeConcern,
                    session -> session.doWork(conn -> conn.createStatement().executeUpdate(EXAMPLE_UPDATE_MQL)));

            // then
            assertCommandsStartedWithExpectedConcerns(readConcern, writeConcern);
        }

        @ParameterizedTest
        @CsvSource({"true, false", "false, true", "true, true"})
        @DisplayName(
                "Transaction level null concerns will inherit the first non-null counterpart from Session to SessionFactory")
        void testTransactionLevelNullConcerns(boolean readConcernIsNull, boolean writeConcernIsNull) {
            // given
            var nonNullSessionReadConcern = ReadConcern.SNAPSHOT;
            var nonNullSessionFactoryWriteConcern =
                    WriteConcern.ACKNOWLEDGED.withW(1).withJournal(true);
            mongoSessionFactory.setReadWriteConcerns(null, nonNullSessionFactoryWriteConcern);

            var nonNullTransactionReadConcern = ReadConcern.MAJORITY;
            var nonNullTransacitonWriteConcern =
                    WriteConcern.ACKNOWLEDGED.withW(1).withJournal(false);

            // when
            mongoSessionFactory.inSession(nonNullSessionReadConcern, null, session -> {
                var mongoSession = session.unwrap(MongoSession.class);
                var transaction = mongoSession.beginTransaction(
                        readConcernIsNull ? null : nonNullTransactionReadConcern,
                        writeConcernIsNull ? null : nonNullTransacitonWriteConcern);
                mongoSession.doWork(conn -> conn.createStatement().executeUpdate(EXAMPLE_UPDATE_MQL));
                transaction.commit();
            });

            // then
            assertCommandsStartedWithExpectedConcerns(
                    readConcernIsNull ? nonNullSessionReadConcern : nonNullTransactionReadConcern,
                    writeConcernIsNull ? nonNullSessionFactoryWriteConcern : nonNullTransacitonWriteConcern);
        }
    }

    private MongoSessionFactory buildSessionFactory(MongoClientCustomizer customizer) throws ServiceException {
        var standardServiceRegistry = new StandardServiceRegistryBuilder()
                .addService(MongoClientCustomizer.class, customizer)
                .build();
        return (MongoSessionFactory) new MetadataSources(standardServiceRegistry)
                .getMetadataBuilder()
                .build()
                .getSessionFactoryBuilder()
                .build();
    }

    private void assertCommandsStartedWithExpectedConcerns(
            @Nullable ReadConcern expectedReadConcern, @Nullable WriteConcern expectedWriteConcern) {
        assertEquals(3, commandsStarted.size()); // the first command is the one in @BeforeEach
        if (expectedReadConcern == null) {
            assertFalse(commandsStarted.get(1).containsKey("readConcern"));
        } else {
            assertEquals(
                    expectedReadConcern.asDocument(), commandsStarted.get(1).getDocument("readConcern"));
        }
        if (expectedWriteConcern == null) {
            assertFalse(commandsStarted.get(2).containsKey("writeConcern"));
        } else {
            assertEquals(
                    expectedWriteConcern.asDocument(), commandsStarted.get(2).getDocument("writeConcern"));
        }
    }

    private static Stream<Arguments> getNonNullConcerns() {
        List<Arguments> arguments = new ArrayList<>();
        @Nullable ReadConcern[] readConcerns = {ReadConcern.LOCAL, ReadConcern.MAJORITY, ReadConcern.SNAPSHOT};
        for (ReadConcern readConcern : readConcerns) {
            arguments.add(Arguments.of(
                    readConcern,
                    WriteConcern.ACKNOWLEDGED
                            .withW(1)
                            .withWTimeout(34, TimeUnit.MILLISECONDS)
                            .withJournal(true)));
        }
        return arguments.stream();
    }
}
