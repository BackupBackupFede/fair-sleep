package net.emeraude.fairsleep;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/** The switch "monsters attack sleepers", when it is off. Called from the mixins and the menu. */
public final class Sleepers {

    /** How far around a sleeping player monsters drop them as a target. */
    private static final double FORGET_RANGE = 64;

    private Sleepers() {}

    /** Whether a monster leaves this target alone because they are asleep. */
    public static boolean ignores(LivingEntity targeter, LivingEntity target) {
        return !FairSleep.config().aggroAllowed && targeter instanceof Enemy && target instanceof Player player && player.isSleeping();
    }

    /** A player just fell asleep: monsters that were already after them give up. */
    public static void fellAsleep(ServerPlayer player) {
        if (!FairSleep.config().aggroAllowed) forget(player);
    }

    /** The switch was just turned off: every player already asleep is dropped too. */
    public static void forgetAll(Iterable<ServerLevel> levels) {
        for (ServerLevel level : levels) {
            for (ServerPlayer player : level.players()) {
                if (player.isSleeping()) forget(player);
            }
        }
    }

    private static void forget(ServerPlayer player) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(FORGET_RANGE), mob -> mob instanceof Enemy)) {
            if (mob.getTarget() == player) mob.setTarget(null);
            Brain<?> brain = mob.getBrain();
            // hasMemoryValue first: getMemory throws on a memory the mob's brain does not have.
            if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == player) {
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
        }
    }
}
