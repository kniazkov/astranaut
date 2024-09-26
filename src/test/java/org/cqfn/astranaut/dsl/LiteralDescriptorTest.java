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
package org.cqfn.astranaut.dsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests covering {@link LiteralDescriptor} class.
 * @since 1.0.0
 */
class LiteralDescriptorTest {
    @Test
    void emptyDescriptor() {
        final LiteralDescriptor.Constructor ctor =
            new LiteralDescriptor.Constructor("Invalid");
        Assertions.assertFalse(ctor.isValid());
        Assertions.assertThrows(IllegalStateException.class, ctor::create);
    }

    @Test
    void nameOnly() {
        final LiteralDescriptor.Constructor ctor =
            new LiteralDescriptor.Constructor("Identifier");
        ctor.setType("String");
        Assertions.assertTrue(ctor.isValid());
        Assertions.assertEquals("Identifier <- 'String', ?, ?, ?, ?", ctor.toString());
        final LiteralDescriptor descriptor = ctor.create();
        Assertions.assertEquals("Identifier <- 'String'", descriptor.toString());
    }
}
