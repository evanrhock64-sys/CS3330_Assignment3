package com.example.haunted.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;


class TrapResolverTest {

    private final TrapResolver resolver = new TrapResolver();
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Hero", 50, 7, 2, new Inventory(1));
    }

    @Test
    void resolveTrap_nullTrap_doesNothing() {
        InteractionResult result = resolver.resolveTrap(player, null);

        assertFalse(result.isSuccess());
        assertEquals("No trap was triggered.", result.getMessage());
        assertEquals(50, player.getHealth());
    }

    @Test
    void resolveTrap_disarmedTrap_doesNothing() {
        Trap trap = new Trap("Wires", TrapType.ELECTRIC, 8, false, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertFalse(result.isSuccess());
        assertEquals("No trap was triggered.", result.getMessage());
        assertEquals(50, player.getHealth());
    }

    @Test
    void resolveTrap_armedOneTimeTrap_damagesAndDisarms() {
        Trap trap = new Trap("Loose Wires Trap", TrapType.ELECTRIC, 8, true, true);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals("Trap 'Loose Wires Trap' triggered for 8 damage.", result.getMessage());
        assertEquals(42, player.getHealth());
        assertFalse(trap.isArmed());
    }

    @Test
    void resolveTrap_armedRepeatingTrap_staysArmed() {
        Trap trap = new Trap("Steam", TrapType.STEAM, 5, true, false);

        resolver.resolveTrap(player, trap);
        resolver.resolveTrap(player, trap);

        assertTrue(trap.isArmed());
        assertEquals(40, player.getHealth());
    }

    @ParameterizedTest(name = "trap damage {0} on 50 health -> {1} health left")
    @CsvSource({"0,50", "1,49", "49,1", "50,0", "51,0", "500,0"})
    void resolveTrap_damageBoundaries(int damage, int expectedHealth) {
        Trap trap = new Trap("Trap", TrapType.STEAM, damage, true, true);

        resolver.resolveTrap(player, trap);

        assertEquals(expectedHealth, player.getHealth());
    }
}
