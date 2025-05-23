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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.cqfn.astranaut.core.algorithms.conversion.Converter;
import org.cqfn.astranaut.core.algorithms.conversion.DefaultTransformer;
import org.cqfn.astranaut.core.base.DefaultFactory;
import org.cqfn.astranaut.core.base.Factory;
import org.cqfn.astranaut.core.base.Provider;
import org.cqfn.astranaut.core.base.Transformer;
import org.cqfn.astranaut.core.base.Type;

/**
 * Program, that is, a set of rules described in DSL.
 * @since 1.0.0
 */
public final class Program implements Provider {
    /**
     * List of all rules (immutable).
     */
    private final List<Rule> all;

    /**
     * Cached result for getAllLanguages().
     */
    private Set<String> languages;

    /**
     * Cached result for getNodeDescriptorsByLanguage().
     */
    private final Map<String, Map<String, NodeDescriptor>> nodes;

    /**
     * Cached result for getTransformationDescriptorsByLanguage().
     */
    private final Map<String, List<TransformationDescriptor>> converters;

    /**
     * Cached result for getFactory().
     */
    private final Map<String, Factory> factories;

    /**
     * Constructor.
     * @param all List of all rules
     */
    public Program(final List<Rule> all) {
        this.all = Collections.unmodifiableList(new ArrayList<>(all));
        this.nodes = new TreeMap<>();
        this.converters = new TreeMap<>();
        this.factories = new TreeMap<>();
    }

    /**
     * Returns all rules in the order they are declared in the DSL file.
     * @return A list of rules.
     */
    public List<Rule> getAllRules() {
        return this.all;
    }

    /**
     * Returns a list of all languages for which at least one rule is described.
     * @return A set containing at least one element
     */
    public Set<String> getAllLanguages() {
        if (this.languages == null) {
            final Set<String> set = new TreeSet<>();
            for (final Rule rule : this.all) {
                set.add(rule.getLanguage());
            }
            this.languages = Collections.unmodifiableSet(set);
        }
        return this.languages;
    }

    /**
     * Returns an immutable map of node descriptors for the specified language.
     * The map is cached for performance.
     * @param language The language to get the node descriptors for
     * @return An immutable map of node descriptors
     */
    public Map<String, NodeDescriptor> getNodeDescriptorsByLanguage(final String language) {
        final Map<String, NodeDescriptor> result;
        if (this.nodes.containsKey(language)) {
            result = this.nodes.get(language);
        } else {
            final Map<String, NodeDescriptor> descriptors = new TreeMap<>();
            for (final Rule rule : this.all) {
                if (rule instanceof NodeDescriptor && rule.getLanguage().equals(language)) {
                    final NodeDescriptor descriptor = (NodeDescriptor) rule;
                    descriptors.put(descriptor.getName(), descriptor);
                }
            }
            result = Collections.unmodifiableMap(descriptors);
            this.nodes.put(language, result);
        }
        return result;
    }

    /**
     * Returns a node descriptor by name and language.
     * If the descriptor is not found in the specified language, it searches in the "common"
     * language (if the specified language is not "common").
     * @param name The name of the node descriptor
     * @param language The language to search in
     * @return The node descriptor, or {@code null} if not found
     */
    public NodeDescriptor getNodeDescriptorByNameAndLanguage(final String name,
        final String language) {
        NodeDescriptor descriptor = this.getNodeDescriptorsByLanguage(language).get(name);
        if (descriptor == null && !"common".equals(language)) {
            descriptor = this.getNodeDescriptorsByLanguage("common").get(name);
        }
        return descriptor;
    }

    /**
     * Returns an immutable list of all transformation descriptors, regardless of language.
     * @return An immutable list of all transformation descriptors.
     */
    public List<TransformationDescriptor> getAllTransformationDescriptors() {
        final List<TransformationDescriptor> descriptors = new ArrayList<>(0);
        for (final Rule rule : this.all) {
            if (rule instanceof TransformationDescriptor) {
                descriptors.add((TransformationDescriptor) rule);
            }
        }
        return Collections.unmodifiableList(descriptors);
    }

    /**
     * Returns an immutable list of transformation descriptors for the specified language.
     * The map is cached for performance.
     * @param language The language to get the transformation descriptors for
     * @return An immutable list of transformation descriptors
     */
    public List<TransformationDescriptor> getTransformationDescriptorsByLanguage(
        final String language) {
        final List<TransformationDescriptor> result;
        if (this.converters.containsKey(language)) {
            result = this.converters.get(language);
        } else {
            final List<TransformationDescriptor> descriptors = new ArrayList<>(0);
            for (final Rule rule : this.all) {
                if (rule instanceof TransformationDescriptor
                    && rule.getLanguage().equals(language)) {
                    final TransformationDescriptor descriptor = (TransformationDescriptor) rule;
                    descriptors.add(descriptor);
                }
            }
            result = Collections.unmodifiableList(descriptors);
            this.converters.put(language, result);
        }
        return result;
    }

    @Override
    public Factory getFactory(final String language) {
        final String lowercase;
        if (language == null || language.isEmpty()) {
            lowercase = "common";
        } else {
            lowercase = language.toLowerCase(Locale.ENGLISH);
        }
        final Factory factory;
        if (this.factories.containsKey(lowercase)) {
            factory = this.factories.get(lowercase);
        } else {
            final Map<String, Type> types = new TreeMap<>();
            for (final Map.Entry<String, NodeDescriptor> entry
                : this.getNodeDescriptorsByLanguage(lowercase).entrySet()) {
                if (entry.getValue() instanceof NonAbstractNodeDescriptor) {
                    types.put(entry.getKey(), (Type) entry.getValue());
                }
            }
            factory = new DefaultFactory(types);
            this.factories.put(lowercase, factory);
        }
        return factory;
    }

    @Override
    public Transformer getTransformer(final String language) {
        final String lowercase;
        if (language == null || language.isEmpty()) {
            lowercase = "common";
        } else {
            lowercase = language.toLowerCase(Locale.ENGLISH);
        }
        return new DefaultTransformer(
            this.getTransformationDescriptorsByLanguage(lowercase)
                .stream()
                .map(d -> (Converter) d).collect(Collectors.toList()),
            this.getFactory(lowercase)
        );
    }
}
