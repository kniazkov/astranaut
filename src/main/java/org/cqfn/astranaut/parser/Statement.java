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

/**
 * Some statement from the DSL source code.
 * @since 1.0.0
 */
public final class Statement {
    /**
     * Name of the file in which the statement is described.
     */
    private String filename;

    /**
     * Number of the first line of the statement.
     */
    private int begin;

    /**
     * Number of the last line of the statement.
     */
    private int end;

    /**
     * Source code of the statement.
     */
    private String code;

    /**
     * Private constructor.
     */
    private Statement() {
    }

    /**
     * Returns the location of the statement as a string.
     * @return Location of the statement
     */
    public String getLocation() {
        final StringBuilder builder = new StringBuilder();
        if (!this.filename.isEmpty()) {
            builder.append(this.filename).append(", ");
        }
        builder.append(this.begin);
        if (this.begin != this.end) {
            builder.append('-').append(this.end);
        }
        return builder.toString();
    }

    /**
     * Returns source code of the statement.
     * @return Source code of the statement
     */
    public String getCode() {
        return this.code
            .replaceAll("[\\r\\n]+", " ")
            .replaceAll("\\s{2,}", " ");
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getLocation()).append(": ").append(this.getCode());
        return builder.toString();
    }

    /**
     * Statement builder.
     * @since 1.0.0
     */
    @SuppressWarnings("PMD.DataClass")
    public static final class Constructor {
        /**
         * Name of the file in which the statement is described.
         */
        private String filename;

        /**
         * Number of the first line of the statement.
         */
        private int begin;

        /**
         * Number of the last line of the statement.
         */
        private int end;

        /**
         * Source code of the statement.
         */
        private String code;

        /**
         * Public constructor.
         */
        public Constructor() {
            this.filename = "";
            this.begin = 0;
            this.end = 0;
            this.code = "";
        }

        /**
         * Sets the name of the file in which the statement is described.
         * @param name Filename
         */
        public void setFilename(final String name) {
            this.filename = name;
        }

        /**
         * Sets the number of the first line of the statement.
         * @param value Line number
         */
        public void setBegin(final int value) {
            this.begin = value;
        }

        /**
         * Sets the number of the last line of the statement.
         * @param value Line number
         */
        public void setEnd(final int value) {
            this.end = value;
        }

        /**
         * Sets the source code of the statement.
         * @param text Source code text
         */
        public void setCode(final String text) {
            this.code = text.trim();
        }

        /**
         * Verifies that all parameters are set and creates a new statement.
         * @return Statement
         */
        public Statement createStatement() {
            if (this.begin <= 0 || this.end <= 0 || this.begin > this.end || this.code.isEmpty()) {
                throw new IllegalStateException();
            }
            final Statement stmt = new Statement();
            stmt.filename = this.filename;
            stmt.begin = this.begin;
            stmt.end = this.end;
            stmt.code = this.code;
            return stmt;
        }
    }
}