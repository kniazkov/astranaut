/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Ivan Kniazkov
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cqfn.astranaut.dsl.Null;
import org.cqfn.astranaut.dsl.ResultingSubtreeDescriptor;
import org.cqfn.astranaut.dsl.RightDataDescriptor;
import org.cqfn.astranaut.dsl.RightSideItem;
import org.cqfn.astranaut.dsl.StaticString;
import org.cqfn.astranaut.dsl.UntypedHole;

/**
 * Parses an item that is part of the right side of a transformation rule or the whole right side
 *  of a transformation rule.
 * @since 1.0.0
 */
public final class RightSideItemParser {
    /**
     * Scanner that produces a sequence of tokens.
     */
    private final Scanner scanner;

    /**
     * Descriptor nesting level.
     */
    private final int nesting;

    /**
     * Count of holes resulting from parsing.
     */
    private final HoleCounter holes;

    /**
     * The last token received from the scanner but not accepted by the parser.
     */
    private Token last;

    /**
     * Constructor.
     * @param scanner Scanner
     * @param nesting Descriptor nesting level
     * @param holes Count of holes resulting from parsing
     */
    public RightSideItemParser(final Scanner scanner, final int nesting, final HoleCounter holes) {
        this.scanner = scanner;
        this.nesting = nesting;
        this.holes = holes;
    }

    /**
     * Parses an item that is part of the right side of a transformation rule or the whole
     *  right side of a transformation rule.
     * @return Root or child element of the resulting subtree, that is, the descriptor or hole;
     *  or {@code null} if the scanner does not contain any more tokens
     * @throws ParsingException If the parse fails
     */
    public RightSideItem parseItem() throws ParsingException {
        final Token first = this.scanner.getToken();
        final RightSideItem item;
        if (first instanceof HashSymbol) {
            item = this.parseNodeHole();
        } else if (first instanceof Identifier) {
            item = this.parseSubtree(first.toString());
        } else if (first instanceof Zero) {
            item = Null.INSTANCE;
            this.parseNull();
        } else if (first == null || first instanceof ClosingRoundBracket && this.nesting > 0) {
            item = null;
        } else {
            throw new InappropriateToken(this.scanner.getLocation(), first);
        }
        return item;
    }

    /**
     * Return the last token received from the scanner but not accepted by the parser.
     * @return Last token or {@code null} if all tokens have been accepted.
     */
    public Token getLastToken() {
        return this.last;
    }

    /**
     * Parses a sequence of tokens as an untyped hole replacing a node.
     * @return An untyped hole
     * @throws ParsingException If the parse fails
     */
    private UntypedHole parseNodeHole() throws ParsingException {
        final Token token = this.scanner.getToken();
        if (!(token instanceof Number)) {
            throw new BadHole(this.scanner.getLocation());
        }
        final int value = ((Number) token).getValue();
        if (!this.holes.hasNodeHole(value)) {
            throw new CommonParsingException(
                this.scanner.getLocation(),
                String.format(
                    "The left side of the rule does not have a corresponding hole numbered #%d replacing a node",
                    value
                )
            );
        }
        return UntypedHole.getInstance(value);
    }

    /**
     * Parses a sequence of tokens as an untyped hole replacing data.
     * @return An untyped hole
     * @throws ParsingException If the parse fails
     */
    private UntypedHole parseDataHole() throws ParsingException {
        final Token token = this.scanner.getToken();
        if (!(token instanceof Number)) {
            throw new BadHole(this.scanner.getLocation());
        }
        final int value = ((Number) token).getValue();
        if (!this.holes.hasDataHole(value)) {
            throw new CommonParsingException(
                this.scanner.getLocation(),
                String.format(
                    "The left side of the rule does not have a corresponding hole numbered #%d replacing data",
                    value
                )
            );
        }
        return UntypedHole.getInstance(value);
    }

    /**
     * Parses a sequence of tokens as a subtree descriptor.
     * @param type The type of the root node of the subtree
     * @return Subtree descriptor
     * @throws ParsingException If the parse fails
     */
    private ResultingSubtreeDescriptor parseSubtree(final String type) throws ParsingException {
        Token next = this.scanner.getToken();
        RightDataDescriptor data = null;
        if (next instanceof OpeningAngleBracket) {
            data = this.parseData();
            next = this.scanner.getToken();
        }
        List<RightSideItem> children = Collections.emptyList();
        if (next instanceof OpeningRoundBracket) {
            children = this.parseChildren();
            next = this.scanner.getToken();
        }
        if (next instanceof ClosingRoundBracket && this.nesting == 0) {
            throw new CommonParsingException(
                this.scanner.getLocation(),
                "Unmatched closing parenthesis ')' found"
            );
        }
        if (next == null && this.nesting > 0) {
            throw new CommonParsingException(
                this.scanner.getLocation(),
                "Unmatched opening parenthesis '(' found"
            );
        }
        this.last = next;
        return new ResultingSubtreeDescriptor(type, data, children);
    }

    /**
     * Parses a sequence of tokens as a data descriptor.
     * @return A data descriptor
     * @throws ParsingException If the parse fails
     */
    private RightDataDescriptor parseData() throws ParsingException {
        final RightDataDescriptor data;
        final Token first = this.scanner.getToken();
        if (first instanceof HashSymbol) {
            data = this.parseDataHole();
        } else if (first instanceof CharSequenceToken) {
            data = new StaticString((CharSequenceToken) first);
        } else {
            throw new CommonParsingException(
                this.scanner.getLocation(),
                "Invalid data inside data descriptor. Expected either a hole ('#...') or a string"
            );
        }
        final Token next = this.scanner.getToken();
        if (!(next instanceof ClosingAngleBracket)) {
            throw new CommonParsingException(
                this.scanner.getLocation(),
                "Closing angle bracket '>' expected for data descriptor"
            );
        }
        return data;
    }

    /**
     * Parses a sequence of tokens as a list of child descriptors.
     * @return List of child descriptors
     * @throws ParsingException If the parse fails
     */
    private List<RightSideItem> parseChildren() throws ParsingException {
        final List<RightSideItem> list = new ArrayList<>(0);
        do {
            final RightSideItemParser parser = new RightSideItemParser(
                this.scanner,
                this.nesting + 1,
                this.holes
            );
            final RightSideItem child = parser.parseItem();
            if (child == null) {
                break;
            }
            list.add(child);
            Token next = parser.getLastToken();
            if (next == null) {
                next = this.scanner.getToken();
            }
            if (next instanceof ClosingRoundBracket) {
                break;
            }
            if (!(next instanceof Comma)) {
                throw new CommonParsingException(
                    this.scanner.getLocation(),
                    "Child descriptors must be separated by commas"
                );
            }
        } while (true);
        return list;
    }

    /**
     * Parses a sequence of tokens as a null token.
     * @throws ParsingException If the parse fails
     */
    private void parseNull() throws ParsingException {
        if (this.nesting > 0) {
            throw new CommonParsingException(
                this.scanner.getLocation(),
                "A 'Null' token cannot be a child node"
            );
        }
        this.last = this.scanner.getToken();
    }
}
