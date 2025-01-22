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

package com.mongodb.hibernate.translate;

import static com.mongodb.hibernate.internal.MongoAssertions.assertNotNull;
import static com.mongodb.hibernate.internal.MongoAssertions.assertNull;
import static com.mongodb.hibernate.internal.MongoAssertions.assertTrue;
import static java.lang.String.format;

import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.Nullable;

/**
 * A data exchange mechanism using shelf analog (with capacity of one space) to overcome the limitation of various
 * visitor methods in {@link org.hibernate.sql.ast.SqlAstWalker} don't return value; Returning values is required during
 * MQL translation (e.g. returning intermediate MQL AST nodes).
 *
 * <p>During one MQL translation process, one single object of this class should be created globally (or not within
 * methods as temporary variable) so various {@code void} visitor methods of {@code SqlAstWalker} could access it as
 * either producer calling {@link #shelve(TypeToken, Object)} method) or consumer calling {@link #unshelve(TypeToken,
 * Runnable)} method). Once the consumer grabs its expected value, it becomes the sole owner of the value with the shelf
 * being emptied.
 *
 * @see org.hibernate.sql.ast.SqlAstWalker
 */
final class SqlAstWalkerShelf {

    private TypeToken<?> typeToken;
    private @Nullable Object value;

    private SqlAstWalkerShelf(TypeToken<?> typeToken) {
        this.typeToken = typeToken;
    }

    public static SqlAstWalkerShelf emptyShelf() {
        return new SqlAstWalkerShelf(new TypeToken<Void>() {});
    }

    /**
     * Grabs the value (matching the expected type) from the shelf.
     *
     * <p>Note that during SQL AST tree traversal, it is common to call this method recursively, so one salient feature
     * of this class is the capability to restore previous state.
     *
     * @param typeToken expected type of the data to be grabbed from shelf.
     * @param shelver the {@code Runnable} wrapper of a void method (e.g. some visitor method of
     *     {@link org.hibernate.sql.ast.SqlAstWalker}) which is supposed to invoke {@link #shelve(TypeToken, Object)}
     *     internally with the provided type
     * @return the grabbed value
     * @param <T> generics type
     */
    @SuppressWarnings("unchecked")
    public <T> T unshelve(TypeToken<T> typeToken, Runnable shelver) {
        var previousType = this.typeToken;
        assertNull(this.value);
        this.typeToken = typeToken;
        try {
            shelver.run();
            return (T) assertNotNull(this.value);
        } finally {
            this.typeToken = previousType;
            this.value = null;
        }
    }

    /**
     * Puts the value onto the shelf matching type expectation.
     *
     * <p>To ensure smooth data exchange, the following pre-conditions need to be satisfied:
     *
     * <ul>
     *   <li>the shelf is empty
     *   <li>the shelf data type matches the data
     * </ul>
     *
     * @param typeToken type provided during shelving; should match the type expectation on shelf.
     * @param value data returned from some void method (e.g. some visitor method of
     *     {@link org.hibernate.sql.ast.SqlAstWalker}.
     * @param <T> generics type
     */
    public <T> void shelve(TypeToken<T> typeToken, T value) {
        assertTrue(
                format("provided type [%s] matches expected type [%s]", typeToken, this.typeToken),
                typeToken.equals(this.typeToken));
        assertNull(this.value);
        this.value = value;
    }
}
