package net.emeraude.fairsleep;

import com.mojang.authlib.GameProfile;
import net.emeraude.fairsleep.compat.Click;
import net.emeraude.fairsleep.compat.ClickRelayMenu;
import net.emeraude.fairsleep.compat.Compat;
import net.emeraude.fairsleep.core.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Behaviour check for a dedicated server, off unless the environment variable
 * {@code <MODID>_SELFTEST=true} is set. Gradle's runServer forks a JVM that inherits the
 * environment, so it works on both loaders without touching any build file.
 *
 * <p>Why this exists: "it compiles" says nothing about a mixin, which resolves its target only at
 * runtime, and "the server started" says nothing about a cold code path. This runs the real thing
 * on players with no connection, checks the outcome, and stops the server — so one command per
 * loader per Minecraft line tells you whether the mod still works.
 *
 * <p>Each scene is built in its own chunk, high in the sky: a stone floor, a bed, and monsters
 * right next to it. The player clicks the bed, as a real one would: the click goes through the
 * same code on every line, where startSleepInBed changed shape three times. The witness replays
 * the scene with the mod switched off: there vanilla must refuse sleep, or the check measures
 * nothing.
 */
public final class FairSleepSelfTest {

    /**
     * Derived from the mod id rather than written out, so the generator has nothing to rename and
     * the name can never drift from what selftest.ps1 sets. For mod id "coolmod": COOLMOD_SELFTEST.
     */
    private static final String SWITCH = FairSleep.MOD_ID.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "") + "_SELFTEST";
    /** High in the sky, away from anything the world generated. */
    private static final int Y = 200;
    /** One scene per chunk, along z = 0..15. */
    private static final BlockPos[] SCENES = {
        new BlockPos(0, Y, 0), new BlockPos(32, Y, 0), new BlockPos(64, Y, 0), new BlockPos(96, Y, 0),
        new BlockPos(128, Y, 0), new BlockPos(160, Y, 0)
    };
    /** Past this many ticks of waiting for the chunks, the test runs anyway and reports what it sees. */
    private static final int MAX_WAIT_TICKS = 600;

    /** The tick the wait started on, or -1 when no test is pending. */
    private static int waitingSince = -1;

    private FairSleepSelfTest() {}

    public static boolean requested() {
        return "true".equalsIgnoreCase(System.getenv(SWITCH));
    }

    /**
     * Forces the scene chunks, then waits: entity queries, which the sleep check is made of, do not
     * see a chunk until a few ticks after it was forced, and 26.x keeps no spawn chunk loaded.
     */
    public static void start(MinecraftServer server) {
        ServerLevel level = server.overworld();
        for (BlockPos scene : SCENES) level.setChunkForced(scene.getX() >> 4, scene.getZ() >> 4, true);
        waitingSince = server.getTickCount();
        FairSleep.LOGGER.info("[SELFTEST] waiting for the scene chunks to load");
    }

    /** Called every server tick; runs the test once every scene chunk ticks entities. */
    public static void tick(MinecraftServer server) {
        if (waitingSince < 0) return;
        ServerLevel level = server.overworld();
        boolean ready = true;
        for (BlockPos scene : SCENES) ready &= level.isPositionEntityTicking(scene);
        int waited = server.getTickCount() - waitingSince;
        if (!ready && waited < MAX_WAIT_TICKS) return;
        waitingSince = -1;
        FairSleep.LOGGER.info("[SELFTEST] chunks {} after {} ticks", ready ? "ready" : "STILL NOT READY", waited);
        run(server);
    }

    private static void run(MinecraftServer server) {
        List<String> failures = new ArrayList<>();
        ServerLevel level = server.overworld();
        Config original = FairSleep.config();
        try {
            night(server, level);
            check(failures, "commands", server.getCommands().getDispatcher().getRoot().getChild(FairSleep.MOD_ID) != null
                ? null : "/fairsleep is not registered");
            check(failures, "settings menu", settingsMenu(server, level));
            check(failures, "monsters at the bed", monstersAtTheBed(server, level, SCENES[0]));
            check(failures, "witness", witness(server, level, SCENES[1]));
            check(failures, "aggro off", aggroOff(server, level, SCENES[2]));
            check(failures, "aggro witness", aggroWitness(server, level, SCENES[3]));
            day(server, level);
            check(failures, "day sleep off", daySleepOff(server, level, SCENES[4]));
            check(failures, "day sleep on", daySleepOn(server, level, SCENES[5]));
        } catch (Throwable t) {
            FairSleep.LOGGER.error("[SELFTEST] crashed", t);
            failures.add("crash: " + t);
        } finally {
            FairSleep.setActive(true);
            FairSleep.useConfig(original);
            FairSleep.saveConfig();
            for (BlockPos scene : SCENES) level.setChunkForced(scene.getX() >> 4, scene.getZ() >> 4, false);
        }
        FairSleep.LOGGER.info("[SELFTEST] RESULT {}", failures.isEmpty() ? "PASS" : "FAIL " + failures);
        server.halt(false);
    }

    private static void check(List<String> failures, String name, String error) {
        if (error == null) {
            FairSleep.LOGGER.info("[SELFTEST] {} OK", name);
        } else {
            FairSleep.LOGGER.error("[SELFTEST] {} FAILED: {}", name, error);
            failures.add(name + ": " + error);
        }
    }

    /**
     * The chest: starts green (aggro allowed), a click on the icon turns it red and off, a click on
     * the pane turns it back; shift-click, throw and number key change nothing and take nothing.
     */
    private static String settingsMenu(MinecraftServer server, ServerLevel level) {
        FairSleep.useConfig(new Config());
        ServerPlayer player = player(server, level);
        SettingsMenu menu = new SettingsMenu(1, player);
        int icon = SettingsMenu.AGGRO, pane = icon + 9;
        if (!id(menu.shown(pane)).equals("minecraft:lime_stained_glass_pane")) return "the pane starts as " + id(menu.shown(pane));

        ClickRelayMenu.click(menu, icon, 0, Click.PICKUP, player);
        if (FairSleep.config().aggroAllowed) return "a click on the icon did not switch aggro off";
        if (!id(menu.shown(pane)).equals("minecraft:red_stained_glass_pane")) return "the pane did not turn red";

        ClickRelayMenu.click(menu, icon, 0, Click.QUICK_MOVE, player);
        ClickRelayMenu.click(menu, icon, 0, Click.THROW, player);
        ClickRelayMenu.click(menu, pane, 0, Click.SWAP, player);
        if (FairSleep.config().aggroAllowed) return "a shift-click, throw or swap switched aggro";
        if (!player.getInventory().isEmpty()) return "an icon ended up in the player's inventory";
        if (menu.shown(icon).isEmpty()) return "the icon left the chest";

        ClickRelayMenu.click(menu, pane, 0, Click.PICKUP, player);
        if (!FairSleep.config().aggroAllowed) return "a click on the pane did not switch aggro back on";
        if (!id(menu.shown(pane)).equals("minecraft:lime_stained_glass_pane")) return "the pane did not turn green again";

        int day = SettingsMenu.DAY_SLEEP, dayPane = day + 9;
        if (!id(menu.shown(dayPane)).equals("minecraft:red_stained_glass_pane")) return "the day-sleep pane starts as " + id(menu.shown(dayPane));
        ClickRelayMenu.click(menu, day, 0, Click.PICKUP, player);
        if (!FairSleep.config().daySleep) return "a click on the clock did not switch day sleep on";
        if (!FairSleep.config().aggroAllowed) return "the day-sleep click switched aggro too";
        if (!id(menu.shown(dayPane)).equals("minecraft:lime_stained_glass_pane")) return "the day-sleep pane did not turn green";
        ClickRelayMenu.click(menu, dayPane, 0, Click.PICKUP, player);
        if (FairSleep.config().daySleep) return "a click on the day-sleep pane did not switch it back off";
        return null;
    }

    /** Day sleep off (the default): vanilla refuses a bed by day. The witness for "day sleep on". */
    private static String daySleepOff(MinecraftServer server, ServerLevel level, BlockPos origin) {
        FairSleep.useConfig(new Config());
        Scene scene = new Scene(level, origin);
        try {
            ServerPlayer player = scene.player(server);
            click(player, scene.head);
            if (player.isSleeping()) return "the player slept by day with the switch off - it is not day, so the day check measures nothing";
            FairSleep.LOGGER.info("[SELFTEST] day witness: with the switch off, vanilla refuses a bed by day, as it should");
            return null;
        } finally {
            scene.clear();
        }
    }

    /**
     * Day sleep on: the player falls asleep by day and stays asleep through their own ticks, where
     * vanilla wakes sleepers at daylight. Switched off again, the next tick wakes them: the witness.
     * doTick, not tick: ServerPlayer.tick skips the entity tick, which the connection runs through
     * doTick, and that is where Player.tick checks the daylight.
     */
    private static String daySleepOn(MinecraftServer server, ServerLevel level, BlockPos origin) {
        Config on = new Config();
        on.daySleep = true;
        FairSleep.useConfig(on);
        Scene scene = new Scene(level, origin);
        try {
            ServerPlayer player = scene.player(server);
            click(player, scene.head);
            if (!player.isSleeping()) return "the player could not sleep by day with the switch on";
            for (int i = 0; i < 3; i++) player.doTick();
            if (!player.isSleeping()) return "daylight woke the player within 3 ticks";
            on.daySleep = false;
            player.doTick();
            if (player.isSleeping()) return "with the switch off a tick of daylight did not wake the player - the stay-asleep check measures nothing";
            return null;
        } finally {
            scene.clear();
        }
    }

    /** A zombie and a skeleton right beside the bed: the player sleeps anyway. */
    private static String monstersAtTheBed(MinecraftServer server, ServerLevel level, BlockPos origin) {
        FairSleep.useConfig(new Config());
        Scene scene = new Scene(level, origin);
        try {
            ServerPlayer player = scene.player(server);
            click(player, scene.head);
            if (!player.isSleeping()) return "the player could not sleep beside a zombie and a skeleton";
            player.stopSleepInBed(true, false);
            return null;
        } finally {
            scene.clear();
        }
    }

    /** The same scene with the mod off: vanilla must refuse sleep, or the check above proves nothing. */
    private static String witness(MinecraftServer server, ServerLevel level, BlockPos origin) {
        FairSleep.setActive(false);
        Scene scene = new Scene(level, origin);
        try {
            ServerPlayer player = scene.player(server);
            click(player, scene.head);
            if (player.isSleeping()) return "with the mod off the player still slept beside the monsters - vanilla never saw them, so the check measures nothing";
            FairSleep.LOGGER.info("[SELFTEST] witness: with the mod off, vanilla refuses sleep, as it should");
            return null;
        } finally {
            FairSleep.setActive(true);
            scene.clear();
        }
    }

    /**
     * Aggro off: the zombie that was after the player drops them when they fall asleep, and neither
     * monster can pick or be given the sleeper as a target. Awake again, the player is fair game.
     */
    private static String aggroOff(MinecraftServer server, ServerLevel level, BlockPos origin) {
        Config off = new Config();
        off.aggroAllowed = false;
        FairSleep.useConfig(off);
        Scene scene = new Scene(level, origin);
        try {
            ServerPlayer player = scene.player(server);
            scene.zombie.setTarget(player);
            if (scene.zombie.getTarget() != player) return "test setup: an awake player cannot be targeted";

            click(player, scene.head);
            if (!player.isSleeping()) return "the player could not fall asleep";
            if (scene.zombie.getTarget() != null) return "the zombie kept the player as its target after they fell asleep";
            if (Compat.canTarget(level, scene.skeleton, player)) return "target conditions let a skeleton pick a sleeping player";
            scene.skeleton.setTarget(player);
            if (scene.skeleton.getTarget() != null) return "setTarget gave a skeleton a sleeping player";

            player.stopSleepInBed(true, false);
            scene.skeleton.setTarget(player);
            if (scene.skeleton.getTarget() != player) return "after waking up the player can no longer be targeted";
            return null;
        } finally {
            scene.clear();
        }
    }

    /** Aggro allowed (the default): a sleeping player stays a target. Otherwise "aggro off" proves nothing. */
    private static String aggroWitness(MinecraftServer server, ServerLevel level, BlockPos origin) {
        FairSleep.useConfig(new Config());
        Scene scene = new Scene(level, origin);
        try {
            ServerPlayer player = scene.player(server);
            scene.zombie.setTarget(player);
            click(player, scene.head);
            if (!player.isSleeping()) return "the player could not fall asleep";
            if (scene.zombie.getTarget() != player) return "with aggro allowed the zombie still dropped the sleeper - the aggro check measures nothing";
            if (!Compat.canTarget(level, scene.skeleton, player)) return "with aggro allowed target conditions still refuse a sleeping player";
            FairSleep.LOGGER.info("[SELFTEST] aggro witness: with aggro allowed, monsters keep the sleeper as a target, as they should");
            player.stopSleepInBed(true, false);
            return null;
        } finally {
            scene.clear();
        }
    }

    // --- helpers --------------------------------------------------------------------------------

    private static String id(ItemStack stack) {
        return String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /**
     * Midnight, by the time command: 26.x keeps time on world clocks, and its overworld beds only
     * work in the dark. The sky darkness behind that test is only refreshed on a tick, hence the
     * explicit update.
     */
    private static void night(MinecraftServer server, ServerLevel level) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set 18000");
        level.updateSkyBrightness();
    }

    /** Mid-morning, the same way. Also clears the weather: a thunderstorm makes beds work by day in vanilla. */
    private static void day(MinecraftServer server, ServerLevel level) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "weather clear");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set 3000");
        level.updateSkyBrightness();
    }

    /** A right click on the bed, empty-handed. */
    private static void click(ServerPlayer player, BlockPos bed) {
        ServerLevel level = (ServerLevel) player.level();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(bed), Direction.UP, bed, false);
        level.getBlockState(bed).useWithoutItem(level, player, hit);
    }

    /** A 16×16 stone floor, a bed facing west, a zombie north of its head and a skeleton south of it. */
    private static final class Scene {
        final ServerLevel level;
        final BlockPos origin;
        final BlockPos head;
        final List<Entity> spawned = new ArrayList<>();
        final Mob zombie;
        final Mob skeleton;

        Scene(ServerLevel level, BlockPos origin) {
            this.level = level;
            this.origin = origin;
            this.head = origin.offset(4, 0, 8);
            removeMobs();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    set(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                    for (int y = 0; y < 5; y++) set(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
            BlockState bed = block("minecraft:red_bed").defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
            set(head, bed.setValue(BlockStateProperties.BED_PART, BedPart.HEAD));
            set(head.east(), bed.setValue(BlockStateProperties.BED_PART, BedPart.FOOT));
            zombie = spawn("minecraft:zombie", head.north());
            skeleton = spawn("minecraft:skeleton", head.south());
        }

        Mob spawn(String id, BlockPos pos) {
            Mob mob = (Mob) Compat.create(entityType(id), level);
            mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(mob);
            spawned.add(mob);
            return mob;
        }

        /** Beside the bed, within vanilla's reach of it. */
        ServerPlayer player(MinecraftServer server) {
            ServerPlayer player = FairSleepSelfTest.player(server, level);
            player.setPos(head.getX() - 0.5, head.getY(), head.getZ() + 0.5);
            return player;
        }

        void clear() {
            for (Entity entity : spawned) entity.discard();
        }

        /** Leftovers of a run that crashed, or monsters that spawned on the floor overnight. */
        private void removeMobs() {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(origin).inflate(24))) mob.discard();
        }

        private void set(BlockPos pos, BlockState state) {
            // 2 | 16: tell clients, but skip neighbour shape updates, which would break a half-built bed.
            level.setBlock(pos, state, 2 | 16);
        }
    }

    /** Blocks resolved by id: the beds became a colour collection in 26.3 (no Blocks.RED_BED). */
    private static Block block(String id) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (String.valueOf(BuiltInRegistries.BLOCK.getKey(block)).equals(id)) return block;
        }
        throw new IllegalArgumentException("unknown block " + id);
    }

    /** Entity types resolved by id: they moved from EntityType to EntityTypes in 26.3. */
    private static EntityType<?> entityType(String id) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(type)).equals(id)) return type;
        }
        throw new IllegalArgumentException("unknown entity type " + id);
    }

    /**
     * A player with a connection that has no channel. Vanilla sends packets on the way (the bed
     * messages, for one), and an unopened Connection simply queues them instead of throwing.
     */
    private static ServerPlayer player(MinecraftServer server, ServerLevel level) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "selftest");
        ServerPlayer player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(profile, false));
        player.getInventory().clearContent();
        return player;
    }
}
