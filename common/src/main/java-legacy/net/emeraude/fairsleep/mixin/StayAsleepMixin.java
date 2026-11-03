package net.emeraude.fairsleep.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.emeraude.fairsleep.DaySleep;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sleep by day: vanilla wakes a sleeper on the first tick of daylight, which would end a day sleep
 * at once. 1.21.1 side, where the check is {@code Level.isDay()} (NeoForge wraps it in an event).
 */
@Mixin(Player.class)
public abstract class StayAsleepMixin {

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    private boolean fairsleep$stayAsleep(boolean day) {
        return day && !DaySleep.on();
    }
}
