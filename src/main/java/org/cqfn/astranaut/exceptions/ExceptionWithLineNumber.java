/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Ivan Kniazkov
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
package org.cqfn.astranaut.exceptions;

import org.cqfn.astranaut.core.exceptions.BaseException;

/**
 * Exception with line number, wraps another exception.
 *
 * @since 0.1.5
 */
public final class ExceptionWithLineNumber extends BaseException {
    private static final long serialVersionUID = 8469819668823203534L;

    /**
     * Base exception.
     */
    private final BaseException base;

    /**
     * Line number.
     */
    private final int number;

    /**
     * Constructor.
     * @param base Base exception
     * @param number Line number
     */
    public ExceptionWithLineNumber(final BaseException base, final int number) {
        this.base = base;
        this.number = number;
    }

    @Override
    public String getInitiator() {
        return this.base.getInitiator();
    }

    @Override
    public String getErrorMessage() {
        return new StringBuilder()
            .append(this.number)
            .append(": ")
            .append(this.base.getErrorMessage())
            .toString();
    }
}
