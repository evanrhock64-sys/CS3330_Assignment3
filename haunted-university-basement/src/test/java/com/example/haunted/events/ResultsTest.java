package com.example.haunted.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.haunted.model.Item;
import com.example.haunted.model.Key;

class ResultsTest {

    @ParameterizedTest(name = "success={0}")
    @ValueSource(booleans = {true, false})
    void interactionResult_storesValues(boolean success) {
        InteractionResult result = new InteractionResult(success, "msg");

        assertEquals(success, result.isSuccess());
        assertEquals("msg", result.getMessage());
    }

    @ParameterizedTest(name = "success={0}")
    @ValueSource(booleans = {true, false})
    void moveResult_storesValues(boolean flag) {
        MoveResult result = new MoveResult(flag, "msg", !flag, 8);

        assertEquals(flag, result.isSuccess());
        assertEquals("msg", result.getMessage());
        assertEquals(!flag, result.isTrapTriggered());
        assertEquals(8, result.getTrapDamage());
    }

    @Test
    void combatResult_storesValues() {
        Item key = new Key("Key", "d");

        CombatResult result = new CombatResult(true, "msg", 6, 4, true, List.of(key));

        assertTrue(result.isSuccess());
        assertEquals("msg", result.getMessage());
        assertEquals(6, result.getDamageToMonster());
        assertEquals(4, result.getDamageToPlayer());
        assertTrue(result.isMonsterDefeated());
        assertSame(key, result.getDroppedItems().get(0));
    }

    @Test
    void combatResult_flagsAreIndependent() {
        CombatResult result = new CombatResult(false, "m", 1, 2, false, List.of());

        assertFalse(result.isSuccess());
        assertFalse(result.isMonsterDefeated());
        assertTrue(result.getDroppedItems().isEmpty());
    }

    @Test
    void combatResult_droppedItems_areProtected() {
        List<Item> source = new ArrayList<>();
        source.add(new Key("Key", "d"));
        CombatResult result = new CombatResult(true, "m", 1, 0, true, source);

        source.add(new Key("Other", "d"));

        assertEquals(1, result.getDroppedItems().size());
        assertThrows(UnsupportedOperationException.class, () -> result.getDroppedItems().clear());
    }
}
