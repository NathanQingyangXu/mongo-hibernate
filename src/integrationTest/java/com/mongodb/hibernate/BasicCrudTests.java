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

package com.mongodb.hibernate;

import static org.hibernate.cfg.JdbcSettings.JAKARTA_JDBC_URL;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import org.bson.BsonDocument;
import org.hibernate.cfg.Configuration;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SessionFactory(exportSchema = false)
@DomainModel(annotatedClasses = BasicCrudTests.Book.class)
class BasicCrudTests {

    @BeforeEach
    void setUp() {
        getMongoCollection().drop();
    }

    @Test
    void test(SessionFactoryScope scope) {
        scope.inTransaction(session -> {
            var book = new Book();
            book.id = 1;
            book.title = "War and Peace";
            book.author = "Leo Tolstoy";
            book.publishYear = 1867;
            session.persist(book);
        });

        var expectedDocuments = Set.of(
                BsonDocument.parse(
                        """
                        {
                            _id: 1,
                            title: "War and Peace",
                            author: "Leo Tolstoy",
                            publishYear: 1867
                        }"""));
        Assertions.assertEquals(expectedDocuments, getMongoCollection().find().into(new HashSet<>()));
    }

    private MongoCollection<BsonDocument> getMongoCollection() {
        var connectionString = new ConnectionString(new Configuration().getProperty(JAKARTA_JDBC_URL));
        try (var mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build())) {
            return mongoClient.getDatabase("mongo-hibernate-test").getCollection("books", BsonDocument.class);
        }
    }

    @Entity(name = "Book")
    @Table(name = "books")
    static class Book {

        @Id
        @Column(name = "_id")
        int id;

        @Nullable String title;

        @Nullable String author;

        int publishYear;
    }
}
