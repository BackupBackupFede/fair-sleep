package net.emeraude.fairsleep.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.emeraude.fairsleep.FairSleep;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Empties vanilla's list of monsters around the bed, so none of them refuses sleep. 1.21.1 side:
 * the query is called on a Level there, on a ServerLevel in 26.x, and the target string must name
 * the owner.
 */
@Mixin(ServerPlayer.class)
public abstract class SleepCheckMixin {

    @ModifyExpressionValue(method = {"startSleepInBed",
        // NeoForge 1.21.1 and 26.2 move the checks into a Supplier lambda, to feed CanPlayerSleepEvent.
        // Its number is lambda$startSleepInBed$13 on one line and $0 on the next, hence a regex;
        // only the lambda that holds the query is injected.
        "/^lambda\\$startSleepInBed\\$\\d+$/"},
        at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private List<Monster> fairsleep$noMonsterCounts(List<Monster> nearby) {
        return FairSleep.monstersThatCount(nearby);
    }
}
