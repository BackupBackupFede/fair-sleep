package net.emeraude.fairsleep.mixin;

import com.mojang.datafixers.util.Either;
import net.emeraude.fairsleep.Sleepers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The end of startSleepInBed, whose parameters differ on every line: the handler takes none of
 * them, only the result, so one handler serves all three.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "startSleepInBed", at = @At("RETURN"))
    private void fairsleep$fellAsleep(CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (cir.getReturnValue().right().isPresent() && player.isSleeping()) Sleepers.fellAsleep(player);
    }
}
