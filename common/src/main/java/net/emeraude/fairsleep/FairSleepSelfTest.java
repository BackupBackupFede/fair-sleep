package net.emeraude.fairsleep;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
 * on a player with no connection, checks the outcome, and stops the server — so one command per
 * loader per Minecraft line tells you whether the mod still works.
 *
 * <p><b>Replace the example check.</b> Assert what YOUR mod does, then keep one habit: after any
 * change, run every combination. And remember what a scripted check cannot see — the gestures a
 * real player makes by accident. Anything touching the inventory needs a pass in a real client too.
 */
public final class FairSleepSelfTest {

    /**
     * Derived from the mod id rather than written out, so the generator has nothing to rename and
     * the name can never drift from what selftest.ps1 sets. For mod id "coolmod": COOLMOD_SELFTEST.
     */
    private static final String SWITCH = FairSleep.MOD_ID.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "") + "_SELFTEST";

    private FairSleepSelfTest() {}

    public static boolean requested() {
        return "true".equalsIgnoreCase(System.getenv(SWITCH));
    }

    public static void run(MinecraftServer server) {
        List<String> failures = new ArrayList<>();
        try {
            ServerLevel level = server.overworld();
            check(failures, "example", example(server, level));
        } catch (Throwable t) {
            FairSleep.LOGGER.error("[SELFTEST] crashed", t);
            failures.add("crash: " + t);
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

    /** EXAMPLE — replace. Opens a menu on a fake player and shift-clicks its result slot. */
    private static String example(MinecraftServer server, ServerLevel level) {
        ServerPlayer player = player(server, level);
        Inventory inventory = player.getInventory();
        StonecutterMenu menu = new StonecutterMenu(1, inventory, ContainerLevelAccess.create(level, player.blockPosition()));
        menu.slots.get(0).set(stack("minecraft:stone", 8));
        menu.clickMenuButton(player, 0);
        if (!menu.slots.get(1).hasItem()) return "vanilla produced no result for 8 stone";

        shiftClick(menu, player, 1);
        int left = menu.slots.get(0).getItem().getCount();
        return left == 0 ? null : left + " stone left in the input (vanilla drains it)";
    }

    // --- helpers worth keeping ------------------------------------------------------------------

    /** What AbstractContainerMenu.doClick does for a shift-click: re-run while the slot refills. */
    private static void shiftClick(AbstractContainerMenu menu, ServerPlayer player, int slot) {
        ItemStack moved = menu.quickMoveStack(player, slot);
        int guard = 0;
        while (!moved.isEmpty() && ItemStack.isSameItem(menu.slots.get(slot).getItem(), moved) && guard++ < 1000) {
            moved = menu.quickMoveStack(player, slot);
        }
    }

    /**
     * A player with a connection that has no channel. Vanilla sends packets on the way (a recipe
     * unlocking, for one), and an unopened Connection simply queues them instead of throwing.
     */
    private static ServerPlayer player(MinecraftServer server, ServerLevel level) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "selftest");
        ServerPlayer player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(profile, false));
        player.getInventory().clearContent();
        return player;
    }

    /** Items resolved by id: several Items.* fields were renamed or regrouped in 26.x. */
    private static ItemStack stack(String id, int count) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (String.valueOf(BuiltInRegistries.ITEM.getKey(item)).equals(id)) return new ItemStack(item, count);
        }
        throw new IllegalArgumentException("unknown item " + id);
    }
}
