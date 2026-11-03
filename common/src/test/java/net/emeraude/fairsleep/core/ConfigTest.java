package net.emeraude.fairsleep.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void aggroIsAllowedByDefault() {
        assertTrue(new Config().aggroAllowed);
    }

    @Test
    void daySleepIsOffByDefault() {
        assertFalse(new Config().daySleep);
    }

    @Test
    void missingFileIsCreatedWithTheDefault(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config").resolve("fairsleep.json");
        Config config = Config.load(file);
        assertTrue(config.aggroAllowed);
        assertFalse(config.daySleep);
        String written = Files.readString(file);
        assertTrue(written.contains("\"aggroAllowed\": true"));
        assertTrue(written.contains("\"daySleep\": false"));
    }

    @Test
    void oldFileWithoutDaySleepGetsIt(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("fairsleep.json");
        Files.writeString(file, "{\"aggroAllowed\": false}");
        Config config = Config.load(file);
        assertFalse(config.aggroAllowed);
        assertFalse(config.daySleep);
        assertTrue(Files.readString(file).contains("\"daySleep\": false"));
    }

    @Test
    void switchIsReadBack(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("fairsleep.json");
        Files.writeString(file, "{\"aggroAllowed\": false}");
        assertFalse(Config.load(file).aggroAllowed);
    }

    @Test
    void saveThenLoadKeepsTheSwitch(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("fairsleep.json");
        Config config = new Config();
        config.aggroAllowed = false;
        config.save(file);
        assertFalse(Config.load(file).aggroAllowed);
    }

    @Test
    void emptyObjectKeepsTheDefault(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("fairsleep.json");
        Files.writeString(file, "{}");
        assertTrue(Config.load(file).aggroAllowed);
    }

    @Test
    void brokenFileIsAnError(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("fairsleep.json");
        Files.writeString(file, "{ not json");
        assertThrows(java.io.IOException.class, () -> Config.load(file));
    }
}
