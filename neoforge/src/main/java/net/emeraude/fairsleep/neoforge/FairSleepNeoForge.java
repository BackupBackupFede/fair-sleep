package net.emeraude.fairsleep.neoforge;

import net.emeraude.fairsleep.FairSleep;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/** NeoForge entry point (both sides). All logic is loader-agnostic in {@link FairSleep}. */
@Mod(FairSleep.MOD_ID)
public final class FairSleepNeoForge {

    public FairSleepNeoForge(IEventBus modBus, ModContainer container) {
        FairSleep.init();
    }
}
