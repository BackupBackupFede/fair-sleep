package net.emeraude.fairsleep.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.emeraude.fairsleep.DaySleep;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ClockTimeMarkers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sleep by day: when everyone is asleep while it is still bright out, the clock moves to the night
 * instead of to the next morning. 26.x side: the target is a time marker, read once in tick both
 * by vanilla and by NeoForge (which hands it to SleepFinishedTimeEvent); 1.21.1 sets a day time.
 */
@Mixin(ServerLevel.class)
public abstract class NightfallMixin {

    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD",
        target = "Lnet/minecraft/world/clock/ClockTimeMarkers;WAKE_UP_FROM_SLEEP:Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<ClockTimeMarker> fairsleep$nightfall(ResourceKey<ClockTimeMarker> morning) {
        return DaySleep.on() && ((ServerLevel) (Object) this).isBrightOutside() ? ClockTimeMarkers.NIGHT : morning;
    }
}
