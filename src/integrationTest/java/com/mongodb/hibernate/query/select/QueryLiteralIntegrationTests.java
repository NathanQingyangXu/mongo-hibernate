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

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.hibernate.testing.orm.junit.DomainModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DomainModel(annotatedClasses = Book.class)
class QueryLiteralIntegrationTests extends AbstractSelectionQueryIntegrationTests {

    private static final List<Book> testingBooks = List.of(
            new Book(1, "War and Peace", null, true, null, 0.2, new BigDecimal("123.50")),
            new Book(2, "Crime and Punishment", 1866, null),
            new Book(3, "Anna Karenina", null, false, 9780310904168L, 0.8, null),
            new Book(4, "The Brothers Karamazov", null, null, null, 0.7, null),
            new Book(5, "War and Peace", 2025, false),
            new Book(6, null, null, null));

    private static List<Book> getBooksByIds(int... ids) {
        return Arrays.stream(ids)
                .mapToObj(id -> testingBooks.stream()
                        .filter(c -> c.id == id)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("id does not exist: " + id)))
                .toList();
    }

    @BeforeEach
    void beforeEach() {
        getSessionFactoryScope().inTransaction(session -> testingBooks.forEach(session::persist));
        getTestCommandListener().clear();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testBoolean(boolean isNullLiteral) {
        var nonNullBooleanLiteralStr = "true";
        assertSelectionQuery(
                format("from Book where outOfStock = %s", isNullLiteral ? null : nonNullBooleanLiteralStr),
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "$and": [
                          {
                            "outOfStock": {
                              "$eq": %s
                            }
                          },
                          {
                            "outOfStock": {
                              "$ne": null
                            }
                          }
                        ]
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "discount": true,
                        "isbn13": true,
                        "outOfStock": true,
                        "price": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }"""
                        .formatted(isNullLiteral ? null : nonNullBooleanLiteralStr),
                isNullLiteral ? emptyList() : getBooksByIds(1));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInteger(boolean isNullLiteral) {
        var nonNullIntegerLiteralStr = "1866";
        assertSelectionQuery(
                format("from Book where publishYear = %s", isNullLiteral ? null : nonNullIntegerLiteralStr),
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "$and": [
                          {
                            "publishYear": {
                              "$eq": %s
                            }
                          },
                          {
                            "publishYear": {
                              "$ne": null
                            }
                          }
                        ]
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "discount": true,
                        "isbn13": true,
                        "outOfStock": true,
                        "price": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }"""
                        .formatted(isNullLiteral ? null : nonNullIntegerLiteralStr),
                isNullLiteral ? emptyList() : getBooksByIds(2));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testLong(boolean isNullLiteral) {
        var nonNullLongLiteralStr = "9780310904168";
        assertSelectionQuery(
                format("from Book where isbn13 = %s", isNullLiteral ? null : (nonNullLongLiteralStr + "L")),
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "$and": [
                          {
                            "isbn13": {
                              "$eq": %s
                            }
                          },
                          {
                            "isbn13": {
                              "$ne": null
                            }
                          }
                        ]
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "discount": true,
                        "isbn13": true,
                        "outOfStock": true,
                        "price": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }"""
                        .formatted(isNullLiteral ? null : nonNullLongLiteralStr),
                isNullLiteral ? emptyList() : getBooksByIds(3));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDouble(boolean isNullLiteral) {
        var nonNullLiteralStr = "0.5";
        assertSelectionQuery(
                format("from Book where discount > %s", isNullLiteral ? null : (nonNullLiteralStr + "D")),
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "$and": [
                          {
                            "discount": {
                              "$gt": %s
                            }
                          },
                          {
                            "discount": {
                              "$ne": null
                            }
                          }
                        ]
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "discount": true,
                        "isbn13": true,
                        "outOfStock": true,
                        "price": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }"""
                        .formatted(isNullLiteral ? null : nonNullLiteralStr),
                isNullLiteral ? emptyList() : getBooksByIds(3, 4));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testString(boolean isNullLiteral) {
        var nonNullLiteralStr = "War and Peace";
        assertSelectionQuery(
                format("from Book where title = %s", isNullLiteral ? null : ("\"" + nonNullLiteralStr + "\"")),
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "$and": [
                          {
                            "title": {
                              "$eq": %s
                            }
                          },
                          {
                            "title": {
                              "$ne": null
                            }
                          }
                        ]
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "discount": true,
                        "isbn13": true,
                        "outOfStock": true,
                        "price": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }"""
                        .formatted(isNullLiteral ? null : ("\"" + nonNullLiteralStr + "\"")),
                isNullLiteral ? emptyList() : getBooksByIds(1, 5));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testBigDecimal(boolean isNullLiteral) {
        var nonNullLiteralStr = "123.50";
        assertSelectionQuery(
                format("from Book where price = %s", isNullLiteral ? null : (nonNullLiteralStr + "BD")),
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "$and": [
                          {
                            "price": {
                              "$eq": %s
                            }
                          },
                          {
                            "price": {
                              "$ne": null
                            }
                          }
                        ]
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "discount": true,
                        "isbn13": true,
                        "outOfStock": true,
                        "price": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }"""
                        .formatted(isNullLiteral ? null : "{\"$numberDecimal\": \"123.50\"}"),
                isNullLiteral ? emptyList() : getBooksByIds(1));
    }
}
