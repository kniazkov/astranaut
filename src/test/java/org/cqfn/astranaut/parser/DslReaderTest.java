/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Ivan Kniazkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.cqfn.astranaut.parser;

import org.cqfn.astranaut.exceptions.BaseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests covering {@link DslReader} class.
 * @since 1.0.0
 */
@SuppressWarnings("PMD.CloseResource")
class DslReaderTest {
    @Test
    void parseString() {
        final String code =
            "This <- 0;\nAddition\n<-\nleft@Expression, right@Expression;Expression <- This | Addition";
        final DslReader reader = new DslReader();
        reader.setSourceCode(code);
        Statement stmt = reader.getStatement();
        Assertions.assertEquals("1: This <- 0", stmt.toString());
        stmt = reader.getStatement();
        Assertions.assertEquals(
            "2-4: Addition <- left@Expression, right@Expression",
            stmt.toString()
        );
        stmt = reader.getStatement();
        Assertions.assertEquals("4: Expression <- This | Addition", stmt.toString());
        stmt = reader.getStatement();
        Assertions.assertNull(stmt);
    }

    @Test
    void readFile() {
        final DslReader reader = new DslReader();
        boolean oops = false;
        try {
            reader.readFile("src/test/resources/dsl/simple.dsl");
        } catch (final BaseException ignored) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Statement stmt = reader.getStatement();
        Assertions.assertEquals("simple.dsl, 5: This <- 0", stmt.toString());
        stmt = reader.getStatement();
        Assertions.assertEquals(
            "simple.dsl, 7-9: Addition <- left@Expression, right@Expression",
            stmt.toString()
        );
        stmt = reader.getStatement();
        Assertions.assertEquals(
            "simple.dsl, 9: Expression <- This | Addition",
            stmt.toString()
        );
        stmt = reader.getStatement();
        Assertions.assertNull(stmt);
    }
}
