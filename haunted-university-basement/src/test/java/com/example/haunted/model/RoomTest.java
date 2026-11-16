package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class RoomTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("hall", "Lecture Hall", "Dusty desks.");
    }

    @Test
    void constructor_setsInitialState() {
        assertEquals("hall", room.getId());
        assertEquals("Lecture Hall", room.getName());
        assertEquals("Dusty desks.", room.getDescription());
        assertTrue(room.getExits().isEmpty());
        assertTrue(room.getItems().isEmpty());
        assertTrue(room.getMonsters().isEmpty());
        assertFalse(room.isLocked());
        assertNull(room.getRequiredKeyName());
        assertNull(room.getTrap());
    }

    @Test
    void constructor_nullArguments_throw() {
        assertThrows(NullPointerException.class, () -> new Room(null, "n", "d"));
        assertThrows(NullPointerException.class, () -> new Room("i", null, "d"));
        assertThrows(NullPointerException.class, () -> new Room("i", "n", null));
    }

    @ParameterizedTest(name = "exit {0}")
    @EnumSource(Direction.class)
    void connect_andGetExit(Direction direction) {
        Room other = new Room("o", "Other", "d");

        room.connect(direction, other);

        assertSame(other, room.getExit(direction));
        for (Direction d : Direction.values()) {
            if (d != direction) {
                assertNull(room.getExit(d));
            }
        }
    }

    @Test
    void connect_sameDirection_replaces() {
        Room first = new Room("a", "A", "d");
        Room second = new Room("b", "B", "d");

        room.connect(Direction.NORTH, first);
        room.connect(Direction.NORTH, second);

        assertSame(second, room.getExit(Direction.NORTH));
        assertEquals(1, room.getExits().size());
    }

    @Test
    void connect_nullArguments_throw() {
        Room other = new Room("o", "Other", "d");

        assertThrows(NullPointerException.class, () -> room.connect(null, other));
        assertThrows(NullPointerException.class, () -> room.connect(Direction.NORTH, null));
    }

    @Test
    void getExits_isUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> room.getExits().put(Direction.NORTH, new Room("o", "O", "d")));
    }

    @ParameterizedTest(name = "lookup \"{0}\"")
    @ValueSource(strings = {"Stapler", "stapler", "STAPLER"})
    void findAndRemoveItem_caseInsensitive(String query) {
        Item stapler = new Weapon("Stapler", "d", 4);
        room.addItem(stapler);

        assertSame(stapler, room.findItem(query).orElseThrow());
        assertSame(stapler, room.removeItemByName(query));
        assertTrue(room.getItems().isEmpty());
    }

    @Test
    void missingItem_behaviour() {
        room.addItem(new Weapon("Stapler", "d", 4));

        assertTrue(room.findItem("Sword").isEmpty());
        assertNull(room.removeItemByName("Sword"));
        assertEquals(1, room.getItems().size());
    }

    @Test
    void items_nullAndUnmodifiable() {
        assertThrows(NullPointerException.class, () -> room.addItem(null));
        assertThrows(UnsupportedOperationException.class, () -> room.getItems().add(new Key("k", "d")));
    }

    @Test
    void monsters_findAndUnmodifiable() {
        Monster ghost = new Monster("Plagiarism Ghost", 22, 8, 2, List.of());
        room.addMonster(ghost);

        assertSame(ghost, room.findMonster("plagiarism ghost").orElseThrow());
        assertTrue(room.findMonster("Nobody").isEmpty());
        assertThrows(NullPointerException.class, () -> room.addMonster(null));
        assertThrows(UnsupportedOperationException.class, () -> room.getMonsters().clear());
    }

    @Test
    void hasLivingMonsters_reflectsState() {
        assertFalse(room.hasLivingMonsters());

        Monster alive = new Monster("A", 5, 1, 1, List.of());
        Monster dead = new Monster("B", 5, 1, 1, List.of());
        dead.takeDamage(5);
        room.addMonster(dead);
        assertFalse(room.hasLivingMonsters());

        room.addMonster(alive);
        assertTrue(room.hasLivingMonsters());

        alive.takeDamage(5);
        assertFalse(room.hasLivingMonsters());
    }

    @Test
    void setLocked_storesState() {
        room.setLocked(true, "Archive Key");

        assertTrue(room.isLocked());
        assertEquals("Archive Key", room.getRequiredKeyName());
    }

    @Test
    void unlock_alreadyUnlocked_returnsTrue() {
        assertTrue(room.unlock("anything"));
        assertTrue(room.unlock(null));
    }

    @ParameterizedTest(name = "key \"{0}\" opens the locked room")
    @ValueSource(strings = {"Archive Key", "archive key", "ARCHIVE KEY"})
    void unlock_correctKey_unlocks(String keyName) {
        room.setLocked(true, "Archive Key");

        assertTrue(room.unlock(keyName));
        assertFalse(room.isLocked());
    }

    @ParameterizedTest(name = "key \"{0}\" does not open the locked room")
    @ValueSource(strings = {"Vault Key", "Archive", "Archive Key ", ""})
    void unlock_wrongKey_staysLocked(String keyName) {
        room.setLocked(true, "Archive Key");

        assertFalse(room.unlock(keyName));
        assertTrue(room.isLocked());
    }

    @Test
    void unlock_nullKey_staysLocked() {
        room.setLocked(true, "Archive Key");

        assertFalse(room.unlock(null));
        assertTrue(room.isLocked());
    }

    @Test
    void unlock_noRequiredKeyName_staysLocked() {
        room.setLocked(true, null);

        assertFalse(room.unlock("Archive Key"));
        assertTrue(room.isLocked());
    }

    @Test
    void setTrap_andClear() {
        Trap trap = new Trap("T", TrapType.STEAM, 3, true, true);

        room.setTrap(trap);
        assertSame(trap, room.getTrap());

        room.setTrap(null);
        assertNull(room.getTrap());
    }
}
