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

import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionDelegatorBaseImpl;
import org.hibernate.engine.spi.SessionImplementor;
import org.jspecify.annotations.Nullable;

public final class MongoSession extends SessionDelegatorBaseImpl implements SessionImplementor {

    private final ReadWriteConcerns readWriteConcerns = new ReadWriteConcerns();

    private @Nullable ReadConcern effectiveCurrentTransactionReadConcern;
    private @Nullable WriteConcern effectiveCurrentTransactionWriteConcern;

    public MongoSession(SessionImplementor delegate) {
        super(delegate);
    }

    void setReadWriteConcerns(@Nullable ReadConcern readConcern, @Nullable WriteConcern writeConcern) {
        readWriteConcerns.setReadConcern(readConcern);
        readWriteConcerns.setWriteConcern(writeConcern);
    }

    void setParentConcerns(@Nullable ReadWriteConcerns readWriteConcerns) {
        this.readWriteConcerns.setParent(readWriteConcerns);
    }

    public @Nullable ReadConcern getEffectiveCurrentTransactionReadConcern() {
        return effectiveCurrentTransactionReadConcern;
    }

    public @Nullable WriteConcern getEffectiveCurrentTransactionWriteConcern() {
        return effectiveCurrentTransactionWriteConcern;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        if (cls.isAssignableFrom(MongoSession.class)) {
            return cls.cast(this);
        }
        return delegate.unwrap(cls);
    }

    @Override
    public Transaction beginTransaction() {
        return beginTransaction(null, null);
    }

    public Transaction beginTransaction(
            @Nullable ReadConcern transactionReadConcern, @Nullable WriteConcern transactinoWriteConcern) {
        effectiveCurrentTransactionReadConcern = readWriteConcerns.getEffectiveReadConcern(transactionReadConcern);
        effectiveCurrentTransactionWriteConcern = readWriteConcerns.getEffectiveWriteConcern(transactinoWriteConcern);
        return super.beginTransaction();
    }
}
