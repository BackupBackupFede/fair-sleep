package net.emeraude.fairsleep.core;

/** When sleepers wake up, in 1.21.1's day-time ticks (26.x does the same with clock markers). */
public final class SleepTime {

    /** Ticks in a Minecraft day. */
    public static final long DAY = 24000;
    /** Where a day sleep ends: just after dusk, when beds work in vanilla and monsters spawn. */
    public static final long NIGHTFALL = 13000;

    private SleepTime() {}

    /**
     * The time sleepers wake up at: vanilla's next morning, or tonight's nightfall when they slept by
     * day with the switch on. Never backwards: a day sleep that ends after nightfall waits for the next.
     */
    public static long wakeTime(long dayTime, long vanillaMorning, boolean sleptByDay) {
        if (!sleptByDay) return vanillaMorning;
        long nightfall = dayTime - Math.floorMod(dayTime, DAY) + NIGHTFALL;
        return nightfall > dayTime ? nightfall : nightfall + DAY;
    }
}
