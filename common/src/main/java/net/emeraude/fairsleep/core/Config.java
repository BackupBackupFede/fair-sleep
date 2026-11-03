package net.emeraude.fairsleep.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Server configuration, read from {@code config/fairsleep.json}. Missing keys keep their default.
 * Also set from {@code /fairsleep settings}.
 */
public final class Config {

    /** Monsters may target and attack a sleeping player, as in vanilla. Off: they leave sleepers alone. */
    public boolean aggroAllowed = true;
    /** Beds work by day too, and sleeping by day brings the night. Off: night only, as in vanilla. */
    public boolean daySleep = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** Reads the file, writing it with every default filled in when it is missing or incomplete. */
    public static Config load(Path file) throws IOException {
        Config config = null;
        if (Files.exists(file)) {
            try {
                config = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Config.class);
            } catch (JsonParseException e) {
                throw new IOException("unreadable " + file + ": " + e.getMessage(), e);
            }
        }
        if (config == null) config = new Config();
        config.save(file);
        return config;
    }

    public void save(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Files.writeString(file, GSON.toJson(this), StandardCharsets.UTF_8);
    }
}
