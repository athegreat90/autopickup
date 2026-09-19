package de.alexandermora.autopickupmod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerAutoPickupModTest {

    @Test
    void modid_constant_matchesExpectedValue() {
        assertEquals("autopickupmod", ServerAutoPickupMod.MODID);
    }
}
