package net.emeraude.fairsleep.fabric;

import net.emeraude.fairsleep.FairSleep;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Fabric entry point (both sides). All logic is loader-agnostic in {@link FairSleep}. */
public final class FairSleepFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FairSleep.init(FabricLoader.getInstance().getConfigDir());
    }
}
