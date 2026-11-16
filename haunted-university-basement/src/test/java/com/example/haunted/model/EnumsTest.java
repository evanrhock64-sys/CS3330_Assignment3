package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnumsTest {

    @Test
    void direction_values() {
        assertArrayEquals(
                new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST},
                Direction.values());
        assertEquals(Direction.EAST, Direction.valueOf("EAST"));
    }

    @Test
    void trapType_values() {
        assertArrayEquals(new TrapType[] {TrapType.ELECTRIC, TrapType.STEAM}, TrapType.values());
        assertEquals(TrapType.STEAM, TrapType.valueOf("STEAM"));
    }

    @Test
    void questStatus_values() {
        assertArrayEquals(
                new QuestStatus[] {QuestStatus.NOT_STARTED, QuestStatus.IN_PROGRESS, QuestStatus.COMPLETED},
                QuestStatus.values());
        assertEquals(QuestStatus.COMPLETED, QuestStatus.valueOf("COMPLETED"));
    }
}
