/*
 * MIT License Copyright (c) 2022 unified-ast
 * https://github.com/unified-ast/ast-generator/blob/master/LICENSE.txt
 */
package org.uast.astgen.codegen.java;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.uast.astgen.rules.Descriptor;
import org.uast.astgen.rules.DescriptorAttribute;

/**
 * Generates 'Matcher' classes.
 * A matcher checks if the node matches some structure, and extracts the data or (and) children.
 *
 * @since 1.0
 */
public final class MatcherGenerator {
    /**
     * The environment.
     */
    private final Environment env;

    /**
     * The package name.
     */
    private final String pkg;

    /**
     * The generator of class names.
     */
    private final ClassNameGenerator names;

    /**
     * The collection in which the generated units are placed.
     */
    private final Map<String, CompilationUnit> units;

    /**
     * Constructor.
     * @param env The environment.
     * @param pkg The package name.
     */
    public MatcherGenerator(final Environment env, final String pkg) {
        this.env = env;
        this.pkg = pkg;
        this.names = new ClassNameGenerator("Matcher");
        this.units = new TreeMap<>();
    }

    /**
     * Generates compilation unit from descriptor.
     * @param descriptor The descriptor
     * @return The name of generated class
     */
    public String generate(final Descriptor descriptor) {
        assert descriptor.getAttribute() == DescriptorAttribute.NONE;
        final String name = this.names.getName();
        final Klass klass = new Klass(
            "Checks if the node matches some structure, and extracts the data and children",
            name
        );
        final CompilationUnit unit = new CompilationUnit(
            this.env.getLicense(),
            this.pkg,
            klass
        );
        this.units.put(name, unit);
        return name;
    }

    /**
     * Returns generated units.
     * @return The collection
     */
    public Map<String, CompilationUnit> getUnits() {
        return Collections.unmodifiableMap(this.units);
    }
}
