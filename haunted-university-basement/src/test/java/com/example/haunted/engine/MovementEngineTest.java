package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.example.haunted.events.MoveResult;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Room;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import com.example.haunted.rules.TrapResolver;

class MovementEngineTest {

    private Room start;
    private Room target;
    private Player player;
    private MovementEngine engine;

    @BeforeEach
    void setUp() {
        start = new Room("start", "Start", "d");
        target = new Room("target", "Target", "d");
        player = new Player("Hero", 50, 7, 2, new Inventory(1));
        player.setCurrentRoom(start);
        engine = new MovementEngine(new TrapResolver());
    }

    @ParameterizedTest(name = "moving {0} with no exit")
    @EnumSource(Direction.class)
    void move_noExit_fails(Direction direction) {
        MoveResult result = engine.move(player, direction);

        assertFalse(result.isSuccess());
        assertEquals("There is no room in that direction.", result.getMessage());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
        assertSame(start, player.getCurrentRoom());
    }

    @ParameterizedTest(name = "moving {0}")
    @EnumSource(Direction.class)
    void move_openExit_succeeds(Direction direction) {
        start.connect(direction, target);

        MoveResult result = engine.move(player, direction);

        assertTrue(result.isSuccess());
        assertEquals("Moved to Target.", result.getMessage());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
        assertSame(target, player.getCurrentRoom());
    }

    @Test
    void move_lockedRoom_fails() {
        start.connect(Direction.NORTH, target);
        target.setLocked(true, "Key");

        MoveResult result = engine.move(player, Direction.NORTH);

        assertFalse(result.isSuccess());
        assertEquals("The room is locked.", result.getMessage());
        assertSame(start, player.getCurrentRoom());
    }

    @Test
    void move_armedTrap_triggers() {
        start.connect(Direction.SOUTH, target);
        target.setTrap(new Trap("Loose Wires Trap", TrapType.ELECTRIC, 8, true, true));

        MoveResult result = engine.move(player, Direction.SOUTH);

        assertTrue(result.isSuccess());
        assertTrue(result.isTrapTriggered());
        assertEquals(8, result.getTrapDamage());
        assertEquals("Moved to Target. Trap 'Loose Wires Trap' triggered for 8 damage.", result.getMessage());
        assertEquals(42, player.getHealth());
        assertSame(target, player.getCurrentRoom());
    }

    @Test
    void move_oneTimeTrap_firesOnce() {
        start.connect(Direction.SOUTH, target);
        target.connect(Direction.NORTH, start);
        target.setTrap(new Trap("T", TrapType.STEAM, 8, true, true));

        engine.move(player, Direction.SOUTH);
        engine.move(player, Direction.NORTH);
        MoveResult second = engine.move(player, Direction.SOUTH);

        assertFalse(second.isTrapTriggered());
        assertEquals("Moved to Target.", second.getMessage());
        assertEquals(42, player.getHealth());
    }

    @Test
    void move_repeatingTrap_firesEveryTime() {
        start.connect(Direction.SOUTH, target);
        target.connect(Direction.NORTH, start);
        target.setTrap(new Trap("T", TrapType.STEAM, 5, true, false));

        engine.move(player, Direction.SOUTH);
        engine.move(player, Direction.NORTH);
        MoveResult second = engine.move(player, Direction.SOUTH);

        assertTrue(second.isTrapTriggered());
        assertEquals(40, player.getHealth());
    }

    @Test
    void move_disarmedTrap_doesNothing() {
        start.connect(Direction.SOUTH, target);
        target.setTrap(new Trap("T", TrapType.STEAM, 8, false, true));

        MoveResult result = engine.move(player, Direction.SOUTH);

        assertTrue(result.isSuccess());
        assertFalse(result.isTrapTriggered());
        assertEquals(50, player.getHealth());
    }
}
