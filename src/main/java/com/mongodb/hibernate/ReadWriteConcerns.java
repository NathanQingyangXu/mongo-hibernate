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
import org.jspecify.annotations.Nullable;

public final class ReadWriteConcerns {

    @Nullable private ReadWriteConcerns parent;

    @Nullable private ReadConcern readConcern;

    @Nullable private WriteConcern writeConcern;

    public ReadWriteConcerns() {}

    public ReadWriteConcerns(@Nullable ReadConcern readConcern, @Nullable WriteConcern writeConcern) {
        this(null, readConcern, writeConcern);
    }

    public ReadWriteConcerns(@Nullable ReadWriteConcerns parent, @Nullable ReadConcern readConcern, @Nullable WriteConcern writeConcern) {
        this.parent = parent;
        this.readConcern = readConcern;
        this.writeConcern = writeConcern;
    }

    public void setParent(@Nullable ReadWriteConcerns parent) {
        this.parent = parent;
    }

    public void setReadConcern(@Nullable ReadConcern readConcern) {
        this.readConcern = readConcern;
    }

    public void setWriteConcern(@Nullable WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }

    @Nullable ReadConcern getEffectiveReadConcern(@Nullable ReadConcern readConcern) {
        if (readConcern != null) {
            return readConcern;
        }
        if (this.readConcern != null) {
            return this.readConcern;
        }
        if (parent != null) {
            return parent.getEffectiveReadConcern(null);
        }
        return null;
    }

    @Nullable WriteConcern getEffectiveWriteConcern(@Nullable WriteConcern writeConcern) {
        if (writeConcern != null) {
            return writeConcern;
        }
        if (this.writeConcern != null) {
            return this.writeConcern;
        }
        if (parent != null) {
            return parent.getEffectiveWriteConcern(null);
        }
        return null;
    }
}
