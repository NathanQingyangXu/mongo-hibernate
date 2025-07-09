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

import static com.mongodb.hibernate.internal.MongoAssertions.assertFalse;
import static com.mongodb.hibernate.internal.MongoAssertions.fail;
import static com.mongodb.hibernate.internal.translate.mongoast.filter.AstLogicalFilterOperator.AND;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.bson.BsonWriter;

public record AstLogicalFilter(AstLogicalFilterOperator operator, List<? extends AstFilter> filters)
        implements AstFilter {

    public AstLogicalFilter {
        assertFalse(filters.isEmpty());
    }

    @Override
    public void render(BsonWriter writer) {
        writer.writeStartDocument();
        {
            writer.writeName(operator.getOperatorName());
            writer.writeStartArray();
            {
                filters.forEach(filter -> filter.render(writer));
            }
            writer.writeEndArray();
        }
        writer.writeEndDocument();
    }

    @Override
    public AstFilter withTernaryNullnessLogicEnforced() {
        return switch (operator) {
            case AND, NOR -> {
                var collectedFieldPathsApplicable = new ArrayList<String>();
                collectTernaryNullnessLogicApplicableFieldPathsRecursively(this, collectedFieldPathsApplicable);
                if (collectedFieldPathsApplicable.isEmpty()) {
                    yield this;
                }
                var dedupedFieldPaths = new LinkedHashSet<>(collectedFieldPathsApplicable)
                        .stream().toList();
                var newFilters = new ArrayList<AstFilter>(1 + dedupedFieldPaths.size());
                newFilters.add(this);
                dedupedFieldPaths.forEach(fieldPath -> newFilters.add(getNullFieldExclusionFilter(fieldPath)));
                yield new AstLogicalFilter(AND, newFilters);
            }
            case OR ->
                new AstLogicalFilter(
                        operator,
                        filters.stream()
                                .map(AstFilter::withTernaryNullnessLogicEnforced)
                                .toList());
        };
    }

    private static void collectTernaryNullnessLogicApplicableFieldPathsRecursively(
            AstFilter astFilter, List<String> collectedFieldPaths) {
        if (astFilter instanceof AstFieldOperationFilter fieldOperationFilter) {
            if (fieldOperationFilter.isTernaryNullnessLogicApplicable()) {
                collectedFieldPaths.add(fieldOperationFilter.fieldPath());
            }
        } else if (astFilter instanceof AstLogicalFilter logicalFilter) {
            logicalFilter
                    .filters()
                    .forEach(filter ->
                            collectTernaryNullnessLogicApplicableFieldPathsRecursively(filter, collectedFieldPaths));
        } else {
            throw fail();
        }
    }
}
