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

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AstVisitorValueHolderTests {

    private AstVisitorValueHolder astVisitorValueHolder;

    @BeforeEach
    void setUp() {
        astVisitorValueHolder = AstVisitorValueHolder.emptyHolder();
    }

    @Test
    void testSimpleUsage() {
        // given

        var valueSet = Set.of("name", "address", "postCode");
        Runnable valueSetter = () -> astVisitorValueHolder.setValue(Set.class, valueSet);

        // when
        var valueGotten = astVisitorValueHolder.getValue(Set.class, valueSetter);

        // then
        assertEquals(valueSet, valueGotten);
    }

    @Test
    void testRecursiveUsage() {
        // given
        var level1Type = Long.class;

        Runnable level1Setter = () -> {
            var level2Type = String.class;
            Runnable level2Setter = () -> {
                var level3Type = List.class;
                Runnable level3Setter = () -> astVisitorValueHolder.setValue(List.class, List.of("name", "address"));
                var level3Value = astVisitorValueHolder.getValue(level3Type, level3Setter);
                astVisitorValueHolder.setValue(level2Type, "fields: " + String.join(",", level3Value));
            };
            var level2Value = astVisitorValueHolder.getValue(level2Type, level2Setter);
            astVisitorValueHolder.setValue(level1Type, level2Value.length() + 20L);
        };

        // when
        var resultLevel1 = astVisitorValueHolder.getValue(level1Type, level1Setter);

        // then
        assertEquals(40L, resultLevel1);
    }

    @Nested
    class ValueSettingTests {

        @Test
        @DisplayName("Exception is thrown when holder is not empty when setting data")
        void testHolderNotEmptyWhenSetting() {
            // given
            Runnable valueSetter = () -> {
                astVisitorValueHolder.setValue(String.class, "first load");
                astVisitorValueHolder.setValue(String.class, "second load");
            };
            // when && then
            assertThrows(Error.class, () -> astVisitorValueHolder.getValue(String.class, valueSetter));
        }

        @Test
        @DisplayName("Exception is thrown when holder is expecting a type different from that of real data")
        void testHolderExpectingDifferentType() {
            // given
            Runnable valueSetter = () -> astVisitorValueHolder.setValue(List.class, List.of(1, 2, 3));

            // when && then
            assertThrows(Error.class, () -> astVisitorValueHolder.getValue(Set.class, valueSetter));
        }
    }

    @Nested
    class ValueGettingTests {

        @Test
        @DisplayName("Exception is thrown when getting value from an empty holder")
        void testHolderStillEmpty() {
            assertThrows(Error.class, () -> astVisitorValueHolder.getValue(String.class, () -> {}));
        }
    }
}
