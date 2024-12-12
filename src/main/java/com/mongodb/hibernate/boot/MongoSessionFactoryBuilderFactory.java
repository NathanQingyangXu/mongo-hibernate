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

package com.mongodb.hibernate.boot;

import com.mongodb.hibernate.MongoSessionFactory;
import com.mongodb.hibernate.jdbc.MongoConnectionProvider;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.spi.AbstractDelegatingSessionFactoryBuilderImplementor;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public final class MongoSessionFactoryBuilderFactory implements SessionFactoryBuilderFactory {

    private static class MongoSessionFactoryBuilderImpl
            extends AbstractDelegatingSessionFactoryBuilderImplementor<SessionFactoryBuilderImplementor>
            implements SessionFactoryBuilderImplementor {

        public MongoSessionFactoryBuilderImpl(SessionFactoryBuilderImplementor delegate) {
            super(delegate);
        }

        @Override
        protected SessionFactoryBuilderImplementor getThis() {
            return this;
        }

        @Override
        public MongoSessionFactory build() {
            var delegate = (SessionFactoryImplementor) super.build();
            var mongoConnectionProvider =
                    (MongoConnectionProvider) delegate.getServiceRegistry().requireService(ConnectionProvider.class);
            var mongoSessionFactory = new MongoSessionFactory(delegate);
            mongoConnectionProvider.setSessionFactory(mongoSessionFactory);
            return mongoSessionFactory;
        }
    }

    @Override
    public SessionFactoryBuilder getSessionFactoryBuilder(
            MetadataImplementor metadata, SessionFactoryBuilderImplementor defaultBuilder) {
        return new MongoSessionFactoryBuilderImpl(defaultBuilder);
    }
}
