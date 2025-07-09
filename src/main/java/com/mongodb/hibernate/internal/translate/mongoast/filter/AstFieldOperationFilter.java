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

package com.mongodb.hibernate.internal.translate.mongoast.filter;

import static com.mongodb.hibernate.internal.translate.mongoast.filter.AstLogicalFilterOperator.AND;

import java.util.List;
import org.bson.BsonWriter;

/**
 * Represents a filter that applies an operation on a field, such as equality or comparison.
 *
 * <p>{@code isTernaryNullnessLogicApplicable} determines whether the filter requires null safety handling to observe
 * HQL/SQL's ternary NULL logic. Some examples of {@code AstFieldOperationFilter} exempted from null safety handling
 * include
 *
 * <ol>
 *   <li>fields annotated with {@code @Id} JPA annotation
 *   <li>fields of primitive Java types
 *   <li>fields with {@code @Column(nullable=false)} JPA annotation
 *   <li>filters corresponding to {@code is null} or {@code is not null} HQL nullness predicate translation
 *   <li>filters created by translation of MQL's <b>find</b> expressions which have inherent nullness robustness already
 *       (e.g. due to <a
 *       href="https://www.mongodb.com/docs/manual/reference/method/db.collection.find/#std-label-type-bracketing">Type
 *       Bracketing</a>).
 * </ol>
 */
public record AstFieldOperationFilter(
        String fieldPath, boolean isTernaryNullnessLogicApplicable, AstFilterOperation filterOperation)
        implements AstFilter {

    @Override
    public void render(BsonWriter writer) {
        writer.writeStartDocument();
        {
            writer.writeName(fieldPath);
            filterOperation.render(writer);
        }
        writer.writeEndDocument();
    }

    @Override
    public AstFilter withTernaryNullnessLogicEnforced() {
        if (!isTernaryNullnessLogicApplicable) {
            return this;
        }
        return new AstLogicalFilter(AND, List.<AstFilter>of(this, getNullFieldExclusionFilter(fieldPath)));
    }
}
