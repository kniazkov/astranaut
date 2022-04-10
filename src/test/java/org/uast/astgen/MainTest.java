/*
 * MIT License Copyright (c) 2022 unified-ast
 * https://github.com/unified-ast/ast-generator/blob/master/LICENSE.txt
 */

package org.uast.astgen;

import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for {@link Main} class.
 *
 * @since 1.0
 */
public class MainTest {
    /**
     * The generate option as an argument example.
     */
    private static final String ARG = "--generate";

    /**
     * The name of the option for a root of the target project.
     */
    private static final String ROOT = "--root";

    /**
     * The name of the option for a package name of generated files.
     */
    private static final String PCG = "--package";

    /**
     * Test passing an option to main().
     *
     * @param source A temporary file
     */
    @Test
    public void testNoException(@TempDir final Path source) throws IOException {
        final Path file = this.createTempDir(source);
        final String[] example = {
            MainTest.ARG,
            file.toString(),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final IllegalArgumentException | IOException exc) {
            caught = true;
        }
        Assertions.assertFalse(caught);
    }

    @Test
    public void testProjectRootOptionNoException(@TempDir final Path source) throws IOException {
        final Path file = this.createTempDir(source);
        final String[] example = {
            MainTest.ARG,
            file.toString(),
            MainTest.ROOT,
            "test/root",
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final ParameterException exc) {
            caught = true;
        }
        Assertions.assertFalse(caught);
    }

    @Test
    public void testProjectRootOptionWithException(@TempDir final Path source) throws IOException {
        final Path file = this.createTempDir(source);
        final String[] example = {
            MainTest.ARG,
            file.toString(),
            MainTest.ROOT,
            "test/ro:ot",
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final ParameterException exc) {
            caught = true;
        }
        Assertions.assertTrue(caught);
    }

    @Test
    public void testProjectRootOptionWithExistingPath(@TempDir final Path source)
        throws IOException {
        final Path file = this.createTempDir(source);
        final String[] example = {
            MainTest.ARG,
            file.toString(),
            MainTest.ROOT,
            source.toString(),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final ParameterException exc) {
            caught = true;
        }
        Assertions.assertFalse(caught);
    }

    @Test
    public void testPackageOptionNoException(@TempDir final Path source) throws IOException {
        final Path file = this.createTempDir(source);
        final String[] example = {
            MainTest.ARG,
            file.toString(),
            MainTest.PCG,
            "org.uast",
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final ParameterException exc) {
            caught = true;
        }
        Assertions.assertFalse(caught);
    }

    @Test
    public void testPackageOptionWithException(@TempDir final Path source) throws IOException {
        final Path file = this.createTempDir(source);
        final String[] example = {
            MainTest.ARG,
            file.toString(),
            MainTest.PCG,
            "org/uast",
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final ParameterException exc) {
            caught = true;
        }
        Assertions.assertTrue(caught);
    }

    /**
     * Test passing no option to main().
     */
    @Test
    public void testWithException() {
        final String[] example = {};
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final ParameterException | IOException exc) {
            caught = true;
        }
        Assertions.assertTrue(caught);
    }

    /**
     * Test passing an option with no parameters to main().
     */
    @Test
    public void testMain() {
        final String[] example = {
            MainTest.ARG,
        };
        boolean caught = false;
        String message = "";
        try {
            Main.main(example);
        } catch (final ParameterException | IOException exc) {
            caught = true;
            message = exc.getMessage();
        }
        Assertions.assertTrue(caught);
        Assertions.assertEquals("Expected a value after parameter --generate", message);
    }

    private Path createTempDir(@TempDir final Path source) throws IOException {
        final Path file = source.resolve("example.txt");
        final List<String> lines = Collections.singletonList("Addition<-Expression, Expression;");
        Files.write(file, lines);
        return file;
    }
}
