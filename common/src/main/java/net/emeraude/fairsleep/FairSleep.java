package net.emeraude.fairsleep;

import net.emeraude.fairsleep.core.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Loader-agnostic entry point — TIER A (server-side) logic goes here.
 *
 * <p>One rule: monsters near the bed no longer stop a player from sleeping. Everything else about
 * beds stays vanilla (night only, not too far, not obstructed). One switch, in the settings chest:
 * whether monsters may attack a sleeping player.
 */
public final class FairSleep {

    public static final String MOD_ID = "fairsleep";
    public static final Logger LOGGER = LoggerFactory.getLogger("Fair Sleep");

    private static boolean initialized = false;
    private static Path configDir = Path.of("config");
    private static Config config = new Config();
    /** Off only for the self-test's witness run, which needs vanilla's refusal to compare against. */
    private static boolean active = true;

    private FairSleep() {}

    /** Called once from each loader's entry point, as early as possible, with the loader's config folder. */
    public static synchronized void init(Path configDir) {
        if (initialized) return;
        initialized = true;
        FairSleep.configDir = configDir;
        reloadConfig();

        LOGGER.info("{} loaded", MOD_ID);
    }

    public static Config config() {
        return config;
    }

    /** Re-reads {@code config/fairsleep.json}. A broken file keeps the previous settings. */
    public static String reloadConfig() {
        try {
            config = Config.load(configDir.resolve(MOD_ID + ".json"));
            return null;
        } catch (IOException e) {
            LOGGER.error("Could not read the config, keeping the previous settings", e);
            return e.getMessage();
        }
    }

    /** Writes the settings changed from the chest. */
    public static String saveConfig() {
        try {
            config.save(configDir.resolve(MOD_ID + ".json"));
            return null;
        } catch (IOException e) {
            LOGGER.error("Could not save the config", e);
            return e.getMessage();
        }
    }

    /** What vanilla's sleep check sees of the monsters around the bed: none. */
    public static <T> List<T> monstersThatCount(List<T> nearby) {
        return active ? List.of() : nearby;
    }

    /** For the self-test only. */
    static void useConfig(Config replacement) {
        config = replacement;
    }

    /** For the self-test's witness run only. */
    static void setActive(boolean on) {
        active = on;
    }
}
