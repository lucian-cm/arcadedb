/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arcadedb.query.sql.method.misc;

import com.arcadedb.query.sql.executor.SQLMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SQLMethodAsBooleanTest {

    private SQLMethod method;

    @BeforeEach
    void setUp() {
        method = new SQLMethodAsBoolean();
    }

    @Test
    void testNulIsReturnedAsNull() {
        Object result = method.execute(null, null, null, null, null);
        assertThat(result).isNull();
    }

    @Test
    void testBooleanReturnedAsBoolean() {
        Object result = method.execute(null, null, null, Boolean.TRUE, null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.TRUE);

        result = method.execute(null, null, null, Boolean.FALSE, null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.FALSE);

        //literal
        result = method.execute(null, null, null, true, null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.TRUE);

        result = method.execute(null, null, null, false, null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.FALSE);
    }

    @Test
    void testStringReturnedAsBoolean() {
        Object result = method.execute(null, null, null, "true", null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.TRUE);

        result = method.execute(null, null, null, "false", null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.FALSE);
    }

    @Test
    void testNumberNotEqualsToZeroReturnedAsTrue() {
        Object result = method.execute(null, null, null, 10, null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.TRUE);

        result = method.execute(null, null, null, -10, null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testNumberEqualsToZeroReturnedAsFalse() {
        Object result = method.execute(null, null, null, 0, null);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(Boolean.FALSE);

    }
}