package net.emeraude.fairsleep;

import net.emeraude.fairsleep.core.SleepTime;

/** The switch "sleep by day", called from the mixins: beds work by day, and a day sleep ends at nightfall. */
public final class DaySleep {

    private DaySleep() {}

    public static boolean on() {
        return FairSleep.config().daySleep;
    }

    /** 1.21.1: the day time sleepers wake at, given vanilla's next morning and whether it is still bright out. */
    public static long wakeTime(long dayTime, long vanillaMorning, boolean bright) {
        return SleepTime.wakeTime(dayTime, vanillaMorning, on() && bright);
    }
}
