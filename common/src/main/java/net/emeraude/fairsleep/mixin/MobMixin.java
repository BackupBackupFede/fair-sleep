package net.emeraude.fairsleep.mixin;

import net.emeraude.fairsleep.Sleepers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Catches the targets set without asking TargetingConditions (retaliation, commands, other mods). */
@Mixin(Mob.class)
public abstract class MobMixin {

    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity fairsleep$sleeperIgnored(LivingEntity target) {
        return Sleepers.ignores((Mob) (Object) this, target) ? null : target;
    }
}
