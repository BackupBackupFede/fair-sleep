package net.emeraude.fairsleep.mixin;

import net.emeraude.fairsleep.FairSleepSelfTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prepares the self-test once the dedicated server's levels exist. No-op unless requested. */
@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    @Inject(method = "initServer", at = @At("RETURN"))
    private void fairsleep$selfTest(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && FairSleepSelfTest.requested()) {
            FairSleepSelfTest.start((MinecraftServer) (Object) this);
        }
    }
}
