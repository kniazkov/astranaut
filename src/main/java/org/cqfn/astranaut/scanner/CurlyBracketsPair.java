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
package org.cqfn.astranaut.scanner;

import org.cqfn.astranaut.exceptions.ParserException;
import org.cqfn.astranaut.rules.DescriptorAttribute;

/**
 * Token that represents a pair of curly brackets and list of tokens between.
 *
 * @since 0.1.5
 */
public class CurlyBracketsPair extends BracketsPair {
    /**
     * Constructor.
     *
     * @param tokens The list of tokens.
     */
    public CurlyBracketsPair(final TokenList tokens) {
        super(tokens);
    }

    @Override
    public final char getOpeningBracket() {
        return '{';
    }

    @Override
    public final char getClosingBracket() {
        return '}';
    }

    @Override
    public final DescriptorAttribute getDescriptorAttribute() throws ParserException {
        return DescriptorAttribute.LIST;
    }
}
