package com.example.haunted.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.example.haunted.engine.GameEngine;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Monster;
import com.example.haunted.model.QuestStatus;
import com.example.haunted.model.Room;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;

class DungeonFactoryTest {

    private static Room walk(Room start, List<Direction> path) {
        Room room = start;
        for (Direction direction : path) {
            room = room.getExit(direction);
            assertNotNull(room, "exit " + direction);
        }
        return room;
    }

    static Stream<Arguments> roomsByPath() {
        Direction n = Direction.NORTH;
        Direction s = Direction.SOUTH;
        Direction e = Direction.EAST;
        Direction w = Direction.WEST;
        return Stream.of(
                Arguments.of(List.of(), "Maintenance Stairwell"),
                Arguments.of(List.of(e), "Abandoned Lecture Hall"),
                Arguments.of(List.of(e, w), "Maintenance Stairwell"),
                Arguments.of(List.of(e, e), "Lab Storage"),
                Arguments.of(List.of(e, s), "Broken Elevator"),
                Arguments.of(List.of(e, n), "Exam Archive"),
                Arguments.of(List.of(e, e, n), "Server Closet"),
                Arguments.of(List.of(e, e, n, e), "Dean Vault"),
                Arguments.of(List.of(e, n, e), "Dean Vault"),
                Arguments.of(List.of(e, n, e, n), "Final Chamber"),
                Arguments.of(List.of(e, n, e, n, s), "Dean Vault"),
                // kills line 56: Exam Archive SOUTH -> Lecture Hall
                Arguments.of(List.of(e, n, s), "Abandoned Lecture Hall"),
                // kills line 59: Dean Vault WEST -> Exam Archive
                Arguments.of(List.of(e, e, n, e, w), "Exam Archive"),
                // kills line 60: Dean Vault SOUTH -> Server Closet
                Arguments.of(List.of(e, n, e, s), "Server Closet"));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("roomsByPath")
    void rooms_areConnectedAsDesigned(List<Direction> path, String expectedName) {
        GameEngine game = DungeonFactory.createGame();

        assertEquals(expectedName, walk(game.getCurrentRoom(), path).getName());
    }

    @Test
    void player_hasDesignedStats() {
        GameEngine game = DungeonFactory.createGame();

        assertEquals("Student Explorer", game.getPlayer().getName());
        assertEquals(50, game.getPlayer().getMaxHealth());
        assertEquals(7, game.getPlayer().getBaseAttack());
        assertEquals(2, game.getPlayer().getBaseDefense());
        assertEquals(8, game.getPlayer().getInventory().getCapacity());
    }

    @Test
    void quest_hasDesignedName() {
        GameEngine game = DungeonFactory.createGame();

        assertEquals("Escape the Basement", game.getQuest().getName());
        assertEquals(QuestStatus.NOT_STARTED, game.getQuest().getStatus());
    }

    @Test
    void lockedRooms_requireDesignedKeys() {
        Room start = DungeonFactory.createGame().getCurrentRoom();
        Room archive = walk(start, List.of(Direction.EAST, Direction.NORTH));
        Room chamber = walk(start, List.of(Direction.EAST, Direction.NORTH, Direction.EAST, Direction.NORTH));
        Room lectureHall = walk(start, List.of(Direction.EAST));

        assertTrue(archive.isLocked());
        assertEquals("Archive Key", archive.getRequiredKeyName());
        assertTrue(chamber.isLocked());
        assertEquals("Vault Key", chamber.getRequiredKeyName());
        assertEquals(false, lectureHall.isLocked());
    }

    @Test
    void brokenElevator_hasTrap() {
        Room start = DungeonFactory.createGame().getCurrentRoom();
        Trap trap = walk(start, List.of(Direction.EAST, Direction.SOUTH)).getTrap();

        assertNotNull(trap);
        assertEquals("Loose Wires Trap", trap.getName());
        assertEquals(TrapType.ELECTRIC, trap.getType());
        assertEquals(8, trap.getDamage());
        assertTrue(trap.isArmed());
        assertTrue(trap.isOneTimeTrigger());
    }

    static Stream<Arguments> monsters() {
        Direction n = Direction.NORTH;
        Direction e = Direction.EAST;
        return Stream.of(
                Arguments.of(List.of(e), "Sleep-Deprived TA", 18, 6, 1),
                Arguments.of(List.of(e, e, n), "Spreadsheet Golem", 28, 7, 4),
                Arguments.of(List.of(e, n), "Plagiarism Ghost", 22, 8, 2),
                Arguments.of(List.of(e, n, e), "Registrar Wraith", 30, 9, 3),
                Arguments.of(List.of(e, n, e, n), "Final Exam Phantom", 40, 10, 4));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("monsters")
    void monsters_haveDesignedStats(List<Direction> path, String name, int health, int attack, int defense) {
        Room room = walk(DungeonFactory.createGame().getCurrentRoom(), path);

        Monster monster = room.findMonster(name).orElseThrow();

        assertEquals(health, monster.getMaxHealth());
        assertEquals(attack, monster.getAttack());
        assertEquals(defense, monster.getDefense());
    }

    @Test
    void phantom_isTheOnlyBoss() {
        Room start = DungeonFactory.createGame().getCurrentRoom();
        Room chamber = walk(start, List.of(Direction.EAST, Direction.NORTH, Direction.EAST, Direction.NORTH));
        Room lectureHall = walk(start, List.of(Direction.EAST));

        assertInstanceOf(BossMonster.class, chamber.findMonster("Final Exam Phantom").orElseThrow());
        assertEquals(Monster.class, lectureHall.findMonster("Sleep-Deprived TA").orElseThrow().getClass());
    }

    @Test
    void items_areInDesignedRooms() {
        Room start = DungeonFactory.createGame().getCurrentRoom();

        assertTrue(walk(start, List.of(Direction.EAST)).findItem("Coffee Potion").isPresent());
        assertTrue(walk(start, List.of(Direction.EAST, Direction.EAST)).findItem("Archive Key").isPresent());
        assertTrue(walk(start, List.of(Direction.EAST, Direction.EAST)).findItem("Calculator Shield").isPresent());
        assertTrue(walk(start, List.of(Direction.EAST, Direction.EAST, Direction.NORTH))
                .findItem("Stapler of Justice").isPresent());
        assertTrue(walk(start, List.of(Direction.EAST, Direction.NORTH)).findItem("Lost Gradebook").isPresent());
        assertTrue(walk(start, List.of(Direction.EAST, Direction.NORTH, Direction.EAST))
                .findItem("Vault Key").isPresent());
    }

    @Test
    void createGame_returnsIndependentGames() {
        GameEngine first = DungeonFactory.createGame();
        GameEngine second = DungeonFactory.createGame();

        first.move(Direction.EAST);
        first.pickUpItem("Coffee Potion");

        assertNotSame(first, second);
        assertEquals("Maintenance Stairwell", second.getCurrentRoom().getName());
        assertTrue(second.getPlayer().getInventory().getItems().isEmpty());
    }

    @Test
    void factory_hasPrivateConstructor() throws Exception {
        Constructor<DungeonFactory> constructor = DungeonFactory.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}
