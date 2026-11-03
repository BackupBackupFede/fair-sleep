package net.emeraude.fairsleep.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.emeraude.fairsleep.Sleepers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The check behind every target goal and attack sensor: with aggro off, a monster does not pick a
 * sleeping player. 26.x added a ServerLevel in front of the two entities; ordinals count
 * LivingEntity parameters only.
 */
@Mixin(TargetingConditions.class)
public abstract class TargetingConditionsMixin {

    @Shadow @Final private boolean isCombat;

    @ModifyReturnValue(method = "test", at = @At("RETURN"))
    private boolean fairsleep$sleeperIgnored(boolean original,
                                             @Local(argsOnly = true, ordinal = 0) LivingEntity targeter,
                                             @Local(argsOnly = true, ordinal = 1) LivingEntity target) {
        return original && !(isCombat && Sleepers.ignores(targeter, target));
    }
}
