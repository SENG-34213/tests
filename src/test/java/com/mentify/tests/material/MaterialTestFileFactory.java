package com.mentify.tests.material;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

final class MaterialTestFileFactory {

    private MaterialTestFileFactory() {
    }

    static TestFile createTextFile() {
        return createFile(".txt", ("Synthetic Mentify integration material " + UUID.randomUUID())
                .getBytes(StandardCharsets.UTF_8));
    }

    static TestFile createEmptyFile() {
        return createFile(".txt", new byte[0]);
    }

    static TestFile createUnsupportedFile() {
        return createFile(".bin", new byte[]{0, 1, 2, 3});
    }

    private static TestFile createFile(String suffix, byte[] content) {
        try {
            Path path = Files.createTempFile("mentify-test-material-", suffix);
            Files.write(path, content);
            return new TestFile(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create temporary material test file", exception);
        }
    }

    static final class TestFile implements AutoCloseable {

        private final Path path;

        private TestFile(Path path) {
            this.path = path;
        }

        Path path() {
            return path;
        }

        @Override
        public void close() {
            try {
                Files.deleteIfExists(path);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to delete temporary material test file", exception);
            }
        }
    }
}
