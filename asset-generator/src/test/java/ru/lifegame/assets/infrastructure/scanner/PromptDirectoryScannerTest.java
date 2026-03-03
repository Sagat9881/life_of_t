package ru.lifegame.assets.infrastructure.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PromptDirectoryScanner}.
 */
class PromptDirectoryScannerTest {

    @TempDir
    Path tempDir;

    private final PromptDirectoryScanner scanner = new PromptDirectoryScanner();

    // -----------------------------------------------------------------------
    // findMissingSpecs
    // -----------------------------------------------------------------------

    @Test
    void findMissingSpecs_allHaveSpecs_returnsEmpty() throws IOException {
        Path chars = Files.createDirectories(tempDir.resolve("characters"));
        Path tanya = Files.createDirectories(chars.resolve("tanya"));
        Files.writeString(tanya.resolve("visual-specs.xml"), "<asset id='x' category='y'/>");

        List<Path> missing = scanner.findMissingSpecs(tempDir);
        assertThat(missing).isEmpty();
    }

    @Test
    void findMissingSpecs_oneEntityMissingSpec_returnsThatDirectory() throws IOException {
        Path chars   = Files.createDirectories(tempDir.resolve("characters"));
        Path tanya   = Files.createDirectories(chars.resolve("tanya"));
        Path husband = Files.createDirectories(chars.resolve("husband"));

        // Only tanya has a spec
        Files.writeString(tanya.resolve("visual-specs.xml"), "<asset id='x' category='y'/>");
        // husband directory is empty (no spec)
        Files.writeString(husband.resolve("dummy.txt"), "placeholder");

        List<Path> missing = scanner.findMissingSpecs(tempDir);
        assertThat(missing).hasSize(1);
        assertThat(missing.get(0)).isEqualTo(husband);
    }

    @Test
    void findMissingSpecs_coreDirectorySkipped() throws IOException {
        // _core directories are skipped (start with '_')
        Path core = Files.createDirectories(tempDir.resolve("_core"));
        Files.writeString(core.resolve("schema.xml"), "...");

        List<Path> missing = scanner.findMissingSpecs(tempDir);
        assertThat(missing).isEmpty();
    }

    // -----------------------------------------------------------------------
    // findAllSpecs
    // -----------------------------------------------------------------------

    @Test
    void findAllSpecs_multipleSpecs_returnsAllPaths() throws IOException {
        Path tanya = Files.createDirectories(
                tempDir.resolve("characters").resolve("tanya"));
        Path home = Files.createDirectories(
                tempDir.resolve("locations").resolve("home"));

        Path spec1 = tanya.resolve("visual-specs.xml");
        Path spec2 = home.resolve("visual-specs.xml");
        Files.writeString(spec1, "<asset id='x' category='y'/>");
        Files.writeString(spec2, "<asset id='y' category='z'/>");

        List<Path> specs = scanner.findAllSpecs(tempDir);
        assertThat(specs).hasSize(2);
        assertThat(specs).contains(spec1, spec2);
    }

    @Test
    void findAllSpecs_emptyRoot_returnsEmpty() throws IOException {
        List<Path> specs = scanner.findAllSpecs(tempDir);
        assertThat(specs).isEmpty();
    }

    @Test
    void findAllSpecs_ignoresNonSpecXmlFiles() throws IOException {
        Path tanya = Files.createDirectories(
                tempDir.resolve("characters").resolve("tanya"));
        Files.writeString(tanya.resolve("other.xml"), "...");

        List<Path> specs = scanner.findAllSpecs(tempDir);
        assertThat(specs).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Edge cases
    // -----------------------------------------------------------------------

    @Test
    void findMissingSpecs_categoryDirectoryNotCountedAsEntity() throws IOException {
        // 'characters' is a category dir (has sub-directories), not a leaf
        Path chars = Files.createDirectories(tempDir.resolve("characters"));
        Path tanya = Files.createDirectories(chars.resolve("tanya"));
        Files.writeString(tanya.resolve("visual-specs.xml"), "<asset id='x' category='y'/>");

        List<Path> missing = scanner.findMissingSpecs(tempDir);
        // 'characters' should NOT be in the missing list
        assertThat(missing).doesNotContain(chars);
    }
}
