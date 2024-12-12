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

package com.mongodb.hibernate;

import static org.hibernate.internal.TransactionManagement.manageTransaction;

import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import java.util.function.Consumer;
import org.hibernate.Session;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.engine.spi.SessionFactoryDelegatingImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.jspecify.annotations.Nullable;

public final class MongoSessionFactory extends SessionFactoryDelegatingImpl implements SessionFactoryImplementor {

    private final ReadWriteConcerns readWriteConcerns = new ReadWriteConcerns();

    public MongoSessionFactory(SessionFactoryImplementor delegate) {
        super(delegate);
    }

    public void setReadWriteConcerns(@Nullable ReadConcern readConcern, @Nullable WriteConcern writeConcern) {
        readWriteConcerns.setReadConcern(readConcern);
        readWriteConcerns.setWriteConcern(writeConcern);
    }

    public void setParentReadWriteConcerns(@Nullable ReadWriteConcerns parent) {
        readWriteConcerns.setParent(parent);
    }

    @Override
    public MongoSession openSession() {
        return openSession(null, null);
    }

    public MongoSession openSession(@Nullable ReadConcern readConcern, @Nullable WriteConcern writeConcern) {
        MongoSession mongoSession = new MongoSession(super.openSession());
        ThreadLocalSessionContext.bind(mongoSession);
        mongoSession.setParentConcerns(readWriteConcerns);
        mongoSession.setReadWriteConcerns(readConcern, writeConcern);
        return mongoSession;
    }

    @Override
    public void inSession(Consumer<Session> action) {
        inSession(null, null, action);
    }

    public void inSession(
            @Nullable ReadConcern readConcern, @Nullable WriteConcern writeConcern, Consumer<Session> action) {
        try (Session session = openSession(readConcern, writeConcern)) {
            action.accept(session);
        }
    }

    @Override
    public void inTransaction(Consumer<Session> action) {
        inTransaction(null, null, action);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        if (cls.isAssignableFrom(MongoSessionFactory.class)) {
            return cls.cast(this);
        }
        return delegate().unwrap(cls);
    }

    public void inTransaction(
            @Nullable ReadConcern readConcern, @Nullable WriteConcern writeConcern, Consumer<Session> action) {
        inSession(session -> manageTransaction(
                session, session.unwrap(MongoSession.class).beginTransaction(readConcern, writeConcern), action));
    }
}
