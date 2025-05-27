/*
 * Copyright 2025-present MongoDB, Inc.
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

package com.mongodb.hibernate.query.select;

import static com.mongodb.hibernate.internal.MongoAssertions.fail;
import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.hibernate.dialect.MongoDialect;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.tree.MutationStatement;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.exec.spi.JdbcOperationQueryMutation;
import org.hibernate.sql.exec.spi.JdbcOperationQuerySelect;
import org.hibernate.sql.model.ast.TableMutation;
import org.hibernate.sql.model.jdbc.JdbcMutationOperation;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.Test;

@DomainModel(annotatedClasses = Book.class)
@ServiceRegistry(
        settings = {
            @Setting(
                    name = Environment.DIALECT,
                    value =
                            "com.mongodb.hibernate.query.select.AffectedTableNamesIntegrationTests$SelectTranslatorAwareDialect")
        })
class AffectedTableNamesIntegrationTests extends AbstractSelectionQueryIntegrationTests {

    @Test
    void testAffectedTableNames() {
        getSessionFactoryScope().inTransaction(session -> {
            session.createSelectionQuery("select b from Book b", Book.class).getResultList();
            var selectAstTranslator =
                    ((SelectTranslatorAwareDialect) session.getJdbcServices().getDialect()).getSelectAstTranslator();
            assertThat(selectAstTranslator.getAffectedTableNames())
                    .singleElement()
                    .satisfies(table -> assertThat(table).isEqualTo("books"));
        });
    }

    public static final class SelectTranslatorAwareDialect extends Dialect {

        private final Dialect delegate;

        private SqlAstTranslator<JdbcOperationQuerySelect> selectAstTranslator;

        public SelectTranslatorAwareDialect(DialectResolutionInfo info) {
            super(info);
            delegate = new MongoDialect(info);
        }

        @Override
        public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
            return new SqlAstTranslatorFactory() {
                @Override
                public SqlAstTranslator<JdbcOperationQuerySelect> buildSelectTranslator(
                        SessionFactoryImplementor sessionFactory, SelectStatement statement) {
                    selectAstTranslator =
                            delegate.getSqlAstTranslatorFactory().buildSelectTranslator(sessionFactory, statement);
                    return selectAstTranslator;
                }

                @Override
                public SqlAstTranslator<? extends JdbcOperationQueryMutation> buildMutationTranslator(
                        SessionFactoryImplementor sessionFactory, MutationStatement statement) {
                    throw fail("buildMutationTranslator() should not be called in this test");
                }

                @Override
                public <O extends JdbcMutationOperation> SqlAstTranslator<O> buildModelMutationTranslator(
                        TableMutation<O> mutation, SessionFactoryImplementor sessionFactory) {
                    return delegate.getSqlAstTranslatorFactory().buildModelMutationTranslator(mutation, sessionFactory);
                }
            };
        }

        public SqlAstTranslator<JdbcOperationQuerySelect> getSelectAstTranslator() {
            return selectAstTranslator;
        }
    }
}
