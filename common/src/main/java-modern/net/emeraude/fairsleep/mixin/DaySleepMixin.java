package net.emeraude.fairsleep.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.emeraude.fairsleep.DaySleep;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sleep by day: a bed that sleeps "when dark" sleeps at any hour while the switch is on. 26.x side,
 * where the bed rule decides; a bed that never sleeps (the Nether's) is left alone.
 */
@Mixin(ServerPlayer.class)
public abstract class DaySleepMixin {

    @WrapOperation(method = {"startSleepInBed",
        // NeoForge 26.2 moves the checks into a Supplier lambda; see SleepCheckMixin.
        "/^lambda\\$startSleepInBed\\$\\d+$/"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z"))
    private boolean fairsleep$byDay(BedRule rule, Level level, Operation<Boolean> original) {
        return (DaySleep.on() && rule.canSleep() == BedRule.Rule.WHEN_DARK) || original.call(rule, level);
    }
}
