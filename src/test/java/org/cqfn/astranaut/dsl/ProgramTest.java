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
package org.cqfn.astranaut.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.cqfn.astranaut.codegen.java.RuleGenerator;
import org.cqfn.astranaut.core.base.Factory;
import org.cqfn.astranaut.core.base.Transformer;
import org.cqfn.astranaut.core.base.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests covering {@link Program} class.
 * @since 1.0.0
 */
class ProgramTest {
    @Test
    void testBaseInterface() {
        final List<Rule> rules = new ArrayList<>(3);
        final NodeDescriptor first = new RegularNodeDescriptor(
            "AAA",
            Collections.emptyList()
        );
        rules.add(first);
        final NodeDescriptor second = new RegularNodeDescriptor(
            "BBB",
            Collections.emptyList()
        );
        second.setLanguage("bbb");
        rules.add(second);
        final Rule third = new TestRule();
        rules.add(third);
        final Program program = new Program(rules);
        final Set<String> languages = new TreeSet<>(Arrays.asList("common", "bbb", "ccc"));
        Assertions.assertEquals(languages, program.getAllLanguages());
        final Collection<NodeDescriptor> descriptors = program
            .getNodeDescriptorsByLanguage("bbb")
            .values();
        Assertions.assertEquals(1, descriptors.size());
        Assertions.assertSame(second, descriptors.iterator().next());
        final Factory factory = program.getFactory(null);
        final Type type = factory.getType("AAA");
        Assertions.assertEquals("AAA", type.getName());
        Assertions.assertSame(factory, program.getFactory(""));
        Transformer transformer = program.getTransformer(null);
        Assertions.assertNotNull(transformer);
        transformer = program.getTransformer("");
        Assertions.assertNotNull(transformer);
    }

    /**
     * Rule for testing purpose.
     * @since 1.0.0
     */
    private static final class TestRule implements Rule {
        @Override
        public String getLanguage() {
            return "ccc";
        }

        @Override
        public void addDependency(final NodeDescriptor descriptor) {
            this.getClass();
        }

        @Override
        public Set<NodeDescriptor> getDependencies() {
            return Collections.emptySet();
        }

        @Override
        public RuleGenerator createGenerator() {
            return null;
        }
    }
}
