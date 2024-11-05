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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.mongodb.client.ClientSession;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoConnectionTests {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // stuff to test "SQLException is to be thrown if connection is closed"

    @FunctionalInterface
    private interface RunnableThrowsSqlException {
        void run() throws SQLException;
    }

    private class TestingSQLExceptionThrownWhenConnectionClosed {

        void testSQLExceptionThrownWhenConnectionClosed(RunnableThrowsSqlException runnableThrowsSqlException)
                throws SQLException {

            // given
            mongoConnection.close();
            verify(clientSession).close();

            // when && then
            assertThrows(SQLException.class, runnableThrowsSqlException::run);
            verifyNoMoreInteractions(clientSession);
        }
    }

    @Mock
    private ClientSession clientSession;

    @InjectMocks
    private MongoConnection mongoConnection;

    @Nested
    class TransactionTests {

        @Nested
        class SetAutoCommitTests extends TestingSQLExceptionThrownWhenConnectionClosed {

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void test(boolean autoCommit) throws SQLException {
                if (autoCommit) {
                    mongoConnection.setAutoCommit(autoCommit);
                    verifyNoInteractions(clientSession);
                } else {
                    mongoConnection.setAutoCommit(autoCommit);
                    verify(clientSession, Mockito.times(1)).startTransaction();
                    assertFalse(mongoConnection.getAutoCommit());
                    mongoConnection.setAutoCommit(autoCommit);
                    verify(clientSession, Mockito.times(1)).startTransaction();
                }
            }

            @Test
            void testSQLExceptionThrownWhenTransactionStartingFailed() {
                doThrow(RuntimeException.class).when(clientSession).startTransaction();
                assertThrows(SQLException.class, () -> mongoConnection.setAutoCommit(false));
            }

            @Test
            void testOldTransactionCommittedWhenStartingNewTransaction() throws SQLException {
                when(clientSession.hasActiveTransaction()).thenReturn(true);
                mongoConnection.setAutoCommit(false);
                var orderVerifier = Mockito.inOrder(clientSession);
                orderVerifier.verify(clientSession).commitTransaction();
                orderVerifier.verify(clientSession).startTransaction();
            }

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void testSetAutoCommitSqlExceptionThrownWhenConnectionClosed(boolean autoCommit) throws SQLException {
                testSQLExceptionThrownWhenConnectionClosed(() -> mongoConnection.setAutoCommit(autoCommit));
            }
        }

        @Nested
        class GetAutoCommitTests extends TestingSQLExceptionThrownWhenConnectionClosed {

            @Test
            void testGetAutoCommitSqlExceptionThrownWhenConnectionClosed() throws SQLException {
                testSQLExceptionThrownWhenConnectionClosed(() -> mongoConnection.getAutoCommit());
            }
        }

        @Nested
        class CommitTests extends TestingSQLExceptionThrownWhenConnectionClosed {

            @Test
            void testSQLExceptionThrownWhenAutoCommitIsTrue() {
                assertThrows(SQLException.class, () -> mongoConnection.commit());
            }

            @Test
            void testSQLExceptionThrownWhenTransactionCommitFailed() throws SQLException {
                mongoConnection.setAutoCommit(false);
                doThrow(RuntimeException.class).when(clientSession).commitTransaction();
                assertThrows(SQLException.class, () -> mongoConnection.commit());
            }

            @Test
            void testCommitSqlExceptionThrownWhenConnectionClosed() throws SQLException {
                testSQLExceptionThrownWhenConnectionClosed(() -> mongoConnection.commit());
            }
        }

        @Nested
        class RollbackTests extends TestingSQLExceptionThrownWhenConnectionClosed {

            @Test
            void testSQLExceptionThrownWhenAutoCommitIsTrue() {
                assertThrows(SQLException.class, () -> mongoConnection.rollback());
            }

            @Test
            void testSQLExceptionThrownWhenTransactionRollbackFailed() throws SQLException {
                mongoConnection.setAutoCommit(false);
                doThrow(RuntimeException.class).when(clientSession).abortTransaction();
                assertThrows(SQLException.class, () -> mongoConnection.rollback());
            }

            @Test
            void testRollbackSqlExceptionThrownWhenConnectionClosed() throws SQLException {
                testSQLExceptionThrownWhenConnectionClosed(() -> mongoConnection.rollback());
            }
        }

        @Nested
        class TransactionIsolationLevelTests extends TestingSQLExceptionThrownWhenConnectionClosed {

            @Test
            void testSetUnsupported() {
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> mongoConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED));
                verifyNoInteractions(clientSession);
            }

            @Test
            void testGetUnsupported() {
                assertThrows(UnsupportedOperationException.class, () -> mongoConnection.getTransactionIsolation());
                verifyNoInteractions(clientSession);
            }

            @Test
            void testGetSqlExceptionThrownWhenConnectionClosed() throws SQLException {
                testSQLExceptionThrownWhenConnectionClosed(() -> mongoConnection.getTransactionIsolation());
            }

            @Test
            void testSetSqlExceptionThrownWhenConnectionClosed() throws SQLException {
                testSQLExceptionThrownWhenConnectionClosed(
                        () -> mongoConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED));
            }
        }
    }
}
