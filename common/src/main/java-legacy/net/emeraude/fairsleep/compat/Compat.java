package net.emeraude.fairsleep.compat;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/** The few calls whose shape differs between 1.21.1 and 26.x. This is the 1.21.1 side. */
public final class Compat {

    private Compat() {}

    public static boolean hasPermission(CommandSourceStack source, int level) {
        return source.hasPermission(level);
    }

    // --- self-test only -------------------------------------------------------------------------

    public static Entity create(EntityType<?> type, ServerLevel level) {
        return type.create(level);
    }

    /** What a hostile mob's target goals ask before picking a target, sight aside. */
    public static boolean canTarget(ServerLevel level, Mob mob, LivingEntity target) {
        return TargetingConditions.forCombat().ignoreLineOfSight().test(mob, target);
    }
}
