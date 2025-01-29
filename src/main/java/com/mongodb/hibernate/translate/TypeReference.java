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

package com.mongodb.hibernate.translate;

import static java.lang.String.format;

import com.mongodb.hibernate.internal.mongoast.AstNode;
import com.mongodb.hibernate.internal.mongoast.AstValue;
import java.util.Arrays;

/**
 * An enum interface denoting the possible types of the value in {@link AstVisitorValueHolder}, so the setter and getter
 * sides could ensure data exchange safety by shariing the same type reference in this class.
 *
 * <p>Note that Java Enum does not support generics. This interface combines both enum and type safety together.
 *
 * @param <T> generics type
 * @see AstVisitorValueHolder
 */
interface TypeReference<T> {
    TypeReference<Void> NULL = new TypeReference<>() {};
    TypeReference<AstNode> COLLECTION_MUTATION = new TypeReference<>() {};
    TypeReference<String> FIELD_NAME = new TypeReference<>() {};
    TypeReference<AstValue> FIELD_VALUE = new TypeReference<>() {};

    default String render() {
        return Arrays.stream(TypeReference.class.getDeclaredFields())
                .filter(field -> {
                    try {
                        return field.get(null) == this;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .findFirst()
                .map(field -> format("%s: %s", field.getGenericType(), field.getName()))
                .orElse(toString());
    }
}
