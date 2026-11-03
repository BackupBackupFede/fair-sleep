package net.emeraude.fairsleep.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.emeraude.fairsleep.DaySleep;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sleep by day: the bed's "it is day" refusal is skipped while the switch is on. 1.21.1 side,
 * where that refusal is {@code Level.isDay()}; 26.x asks the bed rule instead.
 */
@Mixin(ServerPlayer.class)
public abstract class DaySleepMixin {

    @ModifyExpressionValue(method = {"startSleepInBed",
        // NeoForge 1.21.1 moves the checks into a Supplier lambda; see SleepCheckMixin.
        "/^lambda\\$startSleepInBed\\$\\d+$/"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    private boolean fairsleep$byDay(boolean day) {
        return day && !DaySleep.on();
    }
}
