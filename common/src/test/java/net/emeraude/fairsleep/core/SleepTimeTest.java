package net.emeraude.fairsleep.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SleepTimeTest {

    @Test
    void nightSleepKeepsVanillasMorning() {
        assertEquals(24000, SleepTime.wakeTime(18000, 24000, false));
    }

    @Test
    void daySleepWakesAtTonightsNightfall() {
        assertEquals(13000, SleepTime.wakeTime(1000, 24000, true));
        assertEquals(13000, SleepTime.wakeTime(12000, 24000, true));
    }

    @Test
    void laterDaysKeepTheirDate() {
        long day5 = 5 * SleepTime.DAY;
        assertEquals(day5 + 13000, SleepTime.wakeTime(day5 + 6000, day5 + SleepTime.DAY, true));
    }

    @Test
    void neverGoesBackInTime() {
        assertEquals(24000 + 13000, SleepTime.wakeTime(13500, 24000, true));
    }
}
