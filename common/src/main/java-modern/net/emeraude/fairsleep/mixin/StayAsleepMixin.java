package net.emeraude.fairsleep.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.emeraude.fairsleep.DaySleep;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sleep by day: vanilla wakes a sleeper as soon as the bed rule says no, which would end a day
 * sleep at once. 26.x side, where the check is the bed rule (NeoForge wraps it in an event).
 */
@Mixin(Player.class)
public abstract class StayAsleepMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z"))
    private boolean fairsleep$stayAsleep(BedRule rule, Level level, Operation<Boolean> original) {
        return (DaySleep.on() && rule.canSleep() == BedRule.Rule.WHEN_DARK) || original.call(rule, level);
    }
}
