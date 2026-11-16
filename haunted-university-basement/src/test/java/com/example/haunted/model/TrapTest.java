package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrapTest {

    @ParameterizedTest(name = "armed={0}, oneTime={1}")
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void constructor_storesValues(boolean armed, boolean oneTime) {
        Trap trap = new Trap("Loose Wires", TrapType.ELECTRIC, 8, armed, oneTime);

        assertEquals("Loose Wires", trap.getName());
        assertEquals(TrapType.ELECTRIC, trap.getType());
        assertEquals(8, trap.getDamage());
        assertEquals(armed, trap.isArmed());
        assertEquals(oneTime, trap.isOneTimeTrigger());
    }

    @Test
    void constructor_nullArguments_throw() {
        assertThrows(NullPointerException.class, () -> new Trap(null, TrapType.STEAM, 1, true, true));
        assertThrows(NullPointerException.class, () -> new Trap("T", null, 1, true, true));
    }

    @Test
    void disarm_isIdempotent() {
        Trap trap = new Trap("T", TrapType.STEAM, 1, true, false);
        assertTrue(trap.isArmed());

        trap.disarm();
        assertFalse(trap.isArmed());

        trap.disarm();
        assertFalse(trap.isArmed());
    }
}
