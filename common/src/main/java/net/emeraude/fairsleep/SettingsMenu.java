package net.emeraude.fairsleep;

import net.emeraude.fairsleep.compat.Click;
import net.emeraude.fairsleep.compat.ClickRelayMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * {@code /fairsleep settings}: the switches, in a vanilla chest so that no client mod is needed.
 * Each icon has a pane under it, green when on, red when off; a click on either switches it and
 * saves the config.
 *
 * <p>The chest only displays: its slots hold copies, every click is intercepted before vanilla sees
 * it, and the client is resynced afterwards because a vanilla client predicts the move it asked for.
 */
public final class SettingsMenu extends ClickRelayMenu {

    private static final int ROWS = 3, SIZE = ROWS * 9, RULE = 4;
    /** The switches' icons, in the middle row; each pane right under its icon. */
    public static final int AGGRO = 12, DAY_SLEEP = 14;

    private final ServerPlayer viewer;
    private final SimpleContainer display;
    private final ResourceKey<Level> openedIn;

    public SettingsMenu(int id, ServerPlayer viewer) {
        this(id, viewer, new SimpleContainer(SIZE));
    }

    private SettingsMenu(int id, ServerPlayer viewer, SimpleContainer display) {
        super(MenuType.GENERIC_9x3, id, viewer.getInventory(), display, ROWS);
        this.viewer = viewer;
        this.display = display;
        this.openedIn = viewer.level().dimension();
        draw();
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new SettingsMenu(id, player),
            Component.literal("Fair Sleep - while you sleep")));
    }

    public ItemStack shown(int slot) {
        return display.getItem(slot);
    }

    private void draw() {
        for (int i = 0; i < SIZE; i++) display.setItem(i, ItemStack.EMPTY);
        display.setItem(RULE, icon(stack("minecraft:red_bed"), "Fixed rule", ChatFormatting.YELLOW,
            List.of("Monsters nearby never stop you from sleeping")));
        boolean allowed = FairSleep.config().aggroAllowed;
        drawSwitch(AGGRO, "minecraft:zombie_head", "Monsters attack sleepers", "A monster can target and hit a sleeping player",
            allowed, allowed ? "Allowed (vanilla)" : "Not allowed");
        boolean byDay = FairSleep.config().daySleep;
        drawSwitch(DAY_SLEEP, "minecraft:clock", "Sleep by day", "Beds work by day too; sleeping by day brings the night",
            byDay, byDay ? "On" : "Off (vanilla)");
    }

    private void drawSwitch(int slot, String iconId, String title, String what, boolean on, String state) {
        display.setItem(slot, icon(stack(iconId), title, ChatFormatting.WHITE, List.of(what, state)));
        display.setItem(slot + 9, icon(stack(on ? "minecraft:lime_stained_glass_pane" : "minecraft:red_stained_glass_pane"),
            state, on ? ChatFormatting.GREEN : ChatFormatting.RED, List.of(title)));
    }

    @Override
    protected void onClick(int slot, int button, Click click, Player player) {
        try {
            if (player == viewer && click == Click.PICKUP && (button == 0 || button == 1)) {
                if (slot == AGGRO || slot == AGGRO + 9) {
                    boolean allowed = !FairSleep.config().aggroAllowed;
                    FairSleep.config().aggroAllowed = allowed;
                    saved("monsters attacking sleepers", allowed ? "allowed" : "not allowed");
                    if (!allowed) Sleepers.forgetAll(((ServerLevel) viewer.level()).getServer().getAllLevels());
                } else if (slot == DAY_SLEEP || slot == DAY_SLEEP + 9) {
                    boolean byDay = !FairSleep.config().daySleep;
                    FairSleep.config().daySleep = byDay;
                    saved("sleeping by day", byDay ? "on" : "off");
                }
                draw();
            }
        } finally {
            // Undo whatever the client predicted: nothing on the cursor, every slot as the server has it.
            if (viewer.containerMenu == this) {
                setCarried(ItemStack.EMPTY);
                sendAllDataToRemote();
            }
            viewer.inventoryMenu.sendAllDataToRemote();
        }
    }

    private void saved(String what, String value) {
        String failure = FairSleep.saveConfig();
        FairSleep.LOGGER.info("{} set {} to {}", viewer.getName().getString(), what, value);
        if (failure != null) viewer.sendSystemMessage(Component.literal("Not saved: " + failure).withStyle(ChatFormatting.RED));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot slot) {
        return false;
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return false;
    }

    /** Closed by vanilla's per-tick check on death, on a dimension change and on disconnect. */
    @Override
    public boolean stillValid(Player player) {
        return player == viewer && viewer.isAlive() && !viewer.isRemoved() && viewer.level().dimension() == openedIn;
    }

    /** Items resolved by id: several Items.* fields were renamed or regrouped in 26.x. */
    private static ItemStack stack(String id) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (String.valueOf(BuiltInRegistries.ITEM.getKey(item)).equals(id)) return new ItemStack(item);
        }
        throw new IllegalArgumentException("unknown item " + id);
    }

    private static ItemStack icon(ItemStack stack, String name, ChatFormatting colour, List<String> lore) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false).withColor(colour)));
        stack.set(DataComponents.LORE, new ItemLore(lore.stream()
            .map(line -> (Component) Component.literal(line).withStyle(s -> s.withItalic(false).withColor(ChatFormatting.GRAY)))
            .toList()));
        return stack;
    }
}
