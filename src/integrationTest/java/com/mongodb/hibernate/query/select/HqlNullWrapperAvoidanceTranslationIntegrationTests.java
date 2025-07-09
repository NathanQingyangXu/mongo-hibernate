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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;
import org.hibernate.testing.orm.junit.DomainModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@DomainModel(annotatedClasses = HqlNullWrapperAvoidanceTranslationIntegrationTests.Book.class)
public class HqlNullWrapperAvoidanceTranslationIntegrationTests extends AbstractSelectionQueryIntegrationTests {

    private static final List<Book> testingBooks = List.of(
            new Book(1, "I, Robot", 1950),
            new Book(2, "The End of Eternity", 1955),
            new Book(3, "Robot Dreams", 1986),
            new Book(4, "Pebble in the Sky", 1950));

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

    @Test
    void testPrimitiveTypeField() {
        assertSelectionQuery(
                "from Book where publishYear = 1950",
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "publishYear": {
                          "$eq": 1950
                        }
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }""",
                getBooksByIds(1, 4));
    }

    @Test
    void testNonNullableFieldTypeHasNoNullWrapper() {
        assertSelectionQuery(
                "from Book where title = 'Robot Dreams'",
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "title": {
                          "$eq": "Robot Dreams"
                        }
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }""",
                getBooksByIds(3));
    }

    @Test
    void testIdField() {
        assertSelectionQuery(
                "from Book where id = 2",
                Book.class,
                """
                {
                  "aggregate": "books",
                  "pipeline": [
                    {
                      "$match": {
                        "_id": {
                          "$eq": 2
                        }
                      }
                    },
                    {
                      "$project": {
                        "_id": true,
                        "publishYear": true,
                        "title": true
                      }
                    }
                  ]
                }""",
                getBooksByIds(2));
    }

    @Entity(name = "Book")
    @Table(name = "books")
    static class Book {
        @Id
        Integer id;

        @Column(nullable = false)
        String title;

        int publishYear;

        Book() {}

        Book(Integer id, String title, int publishYear) {
            this.id = id;
            this.title = title;
            this.publishYear = publishYear;
        }
    }
}
