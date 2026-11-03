package net.emeraude.fairsleep.mixin;

import net.emeraude.fairsleep.FairSleepSelfTest;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Runs the self-test once its chunks see entities, a few ticks after start. No-op otherwise. */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void fairsleep$selfTest(CallbackInfo ci) {
        FairSleepSelfTest.tick((MinecraftServer) (Object) this);
    }
}
