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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.reflect.TypeToken;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SqlAstWalkerShelfTests {

    private SqlAstWalkerShelf sqlAstWalkerShelf;

    @BeforeEach
    void setUp() {
        sqlAstWalkerShelf = SqlAstWalkerShelf.emptyShelf();
    }

    @Test
    void testSuccess() {
        // given
        TypeToken<Set<String>> typeToken = new TypeToken<Set<String>>() {};

        var data = Set.of("name", "address", "postCode");
        Runnable visit = () -> sqlAstWalkerShelf.shelve(typeToken, data);

        // when
        var result = sqlAstWalkerShelf.unshelve(typeToken, visit);

        // then
        assertEquals(data, result);
    }

    @Test
    void testRecursivelyShelveSuccess() {
        // given
        var typeLevel1 = new TypeToken<Long>() {};

        Runnable shelverLevel1 = () -> {
            var typeLevel2 = new TypeToken<String>() {};
            Runnable shelverLevel2 = () -> {
                var typeLevel3 = new TypeToken<List<String>>() {};
                Runnable shelverLevel3 = () -> sqlAstWalkerShelf.shelve(typeLevel3, List.of("name", "address"));
                var valueLevel3 = sqlAstWalkerShelf.unshelve(typeLevel3, shelverLevel3);
                sqlAstWalkerShelf.shelve(typeLevel2, "fields: " + String.join(",", valueLevel3));
            };
            var level2Value = sqlAstWalkerShelf.unshelve(typeLevel2, shelverLevel2);
            sqlAstWalkerShelf.shelve(typeLevel1, level2Value.length() + 20L);
        };

        // when
        var resultLevel1 = sqlAstWalkerShelf.unshelve(typeLevel1, shelverLevel1);

        // then
        assertEquals(40L, resultLevel1);
    }

    @Nested
    class ShelvingTests {

        @Test
        @DisplayName("Exception is thrown when shelf is occupied when shelving new data")
        void testShelfOccupiedWhenShelving() {
            // given
            var type = new TypeToken<String>() {};
            Runnable shelver = () -> {
                sqlAstWalkerShelf.shelve(type, "first load");
                sqlAstWalkerShelf.shelve(type, "second load");
            };
            // when && then
            assertThrows(Error.class, () -> sqlAstWalkerShelf.unshelve(type, shelver));
        }

        @Test
        @DisplayName("Exception is thrown when shelf data type mismatches shelved data")
        void testShelfExpectingDifferentType() {
            // given
            Runnable shelver = () -> sqlAstWalkerShelf.shelve(new TypeToken<>() {}, List.of(1, 2, 3));

            // when && then
            assertThrows(Error.class, () -> sqlAstWalkerShelf.unshelve(new TypeToken<List<String>>() {}, shelver));
        }
    }

    @Nested
    class UnshelvingTests {

        @Test
        @DisplayName("Exception is thrown when shelf is still empty")
        void testShelfStillEmpty() {
            // given
            var type = new TypeToken<String>() {};

            // when && then
            assertThrows(Error.class, () -> sqlAstWalkerShelf.unshelve(type, () -> {}));
        }
    }
}
