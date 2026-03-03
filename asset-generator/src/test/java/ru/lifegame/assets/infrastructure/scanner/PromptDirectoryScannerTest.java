package ru.lifegame.assets.infrastructure.scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PromptDirectoryScanner — обнаружение entity и missing specs")
class PromptDirectoryScannerTest {

    private PromptDirectoryScanner scanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = new PromptDirectoryScanner();
    }

    @Test
    @DisplayName("Обнаруживает entity-директории под characters/ и locations/")
    void discoverEntities() throws IOException {
        Files.createDirectories(tempDir.resolve("characters/tanya"));
        Files.createDirectories(tempDir.resolve("characters/husband"));
        Files.createDirectories(tempDir.resolve("locations/home"));

        List<Path> entities = scanner.discoverEntities(tempDir);
        assertThat(entities).hasSize(3);
    }

    @Test
    @DisplayName("Игнорирует файлы (не директории) внутри entity-type")
    void ignoresFiles() throws IOException {
        Files.createDirectories(tempDir.resolve("characters/tanya"));
        Files.createFile(tempDir.resolve("characters/some-file.txt"));

        List<Path> entities = scanner.discoverEntities(tempDir);
        assertThat(entities).hasSize(1);
    }

    @Test
    @DisplayName("Игнорирует неизвестные типовые директории")
    void ignoresUnknownTypeDirs() throws IOException {
        Files.createDirectories(tempDir.resolve("characters/tanya"));
        Files.createDirectories(tempDir.resolve("unknown_type/something"));

        List<Path> entities = scanner.discoverEntities(tempDir);
        assertThat(entities).hasSize(1);
    }

    @Test
    @DisplayName("findMissingSpecs: entity без visual-specs.xml → missing")
    void findMissingSpecs_noXml() throws IOException {
        Files.createDirectories(tempDir.resolve("characters/tanya"));
        Files.createDirectories(tempDir.resolve("characters/husband"));

        List<Path> missing = scanner.findMissingSpecs(tempDir);
        assertThat(missing).hasSize(2);
    }

    @Test
    @DisplayName("findMissingSpecs: entity с visual-specs.xml → не missing")
    void findMissingSpecs_withXml() throws IOException {
        Path tanyaDir = tempDir.resolve("characters/tanya");
        Files.createDirectories(tanyaDir);
        Files.createFile(tanyaDir.resolve("visual-specs.xml"));

        Path husbandDir = tempDir.resolve("characters/husband");
        Files.createDirectories(husbandDir);

        List<Path> missing = scanner.findMissingSpecs(tempDir);
        assertThat(missing).hasSize(1);
        assertThat(missing.get(0).getFileName().toString()).isEqualTo("husband");
    }

    @Test
    @DisplayName("extractEntityType возвращает тип из пути")
    void extractEntityType() throws IOException {
        Path entityDir = tempDir.resolve("characters/tanya");
        Files.createDirectories(entityDir);

        String type = scanner.extractEntityType(entityDir);
        assertThat(type).isEqualTo("characters");
    }

    @Test
    @DisplayName("extractEntityName возвращает имя из пути")
    void extractEntityName() throws IOException {
        Path entityDir = tempDir.resolve("locations/home");
        Files.createDirectories(entityDir);

        String name = scanner.extractEntityName(entityDir);
        assertThat(name).isEqualTo("home");
    }

    @Test
    @DisplayName("Пустой prompts root → пустой результат")
    void emptyRoot() {
        List<Path> entities = scanner.discoverEntities(tempDir);
        assertThat(entities).isEmpty();
    }

    @Test
    @DisplayName("discoverEntities находит pets/")
    void discoverPets() throws IOException {
        Files.createDirectories(tempDir.resolve("pets/garfield"));

        List<Path> entities = scanner.discoverEntities(tempDir);
        assertThat(entities).hasSize(1);
    }
}
