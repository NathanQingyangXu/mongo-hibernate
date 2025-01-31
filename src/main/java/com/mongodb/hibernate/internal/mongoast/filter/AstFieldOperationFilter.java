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

package com.mongodb.hibernate.internal.mongoast.filter;

import static com.mongodb.hibernate.internal.mongoast.filter.AstComparisonFilterOperator.EQ;

import org.bson.BsonWriter;

/**
 * An {@link AstFilter} based on {@link AstFilterOperation} on some {@link AstFilterField}.
 *
 * <p>This class is not part of the public API and may be removed or changed at any time
 *
 * @param field filter object as some field
 * @param operation filter operation on the {@code field}
 */
public record AstFieldOperationFilter(AstFilterField field, AstFilterOperation operation) implements AstFilter {
    @Override
    public void render(BsonWriter writer) {
        writer.writeStartDocument();
        field.render(writer);
        operation.render(writer);
        writer.writeEndDocument();
    }

    @Override
    public boolean isIdEqualityFilter() {
        return "_id".equals(field.path())
                && operation instanceof AstComparisonFilterOperation comparisonFilterOperation
                && EQ.equals(comparisonFilterOperation.operator());
    }
}
