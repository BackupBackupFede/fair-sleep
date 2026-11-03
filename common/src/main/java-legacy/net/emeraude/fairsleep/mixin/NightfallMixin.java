package net.emeraude.fairsleep.mixin;

import net.emeraude.fairsleep.DaySleep;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Sleep by day: when everyone is asleep while it is still bright out, the night comes instead of
 * the next morning. 1.21.1 side: vanilla sets the morning with {@code setDayTime} (NeoForge passes
 * it through SleepFinishedTimeEvent first); 26.x moves a clock to a time marker.
 */
@Mixin(ServerLevel.class)
public abstract class NightfallMixin {

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    private long fairsleep$nightfall(long morning) {
        ServerLevel level = (ServerLevel) (Object) this;
        return DaySleep.wakeTime(level.getDayTime(), morning, level.isDay());
    }
}
