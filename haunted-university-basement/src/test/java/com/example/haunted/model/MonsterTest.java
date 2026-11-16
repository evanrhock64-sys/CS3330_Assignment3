package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MonsterTest {

    @Test
    void constructor_setsStats() {
        Monster monster = new Monster("TA", 18, 6, 1, List.of());

        assertEquals("TA", monster.getName());
        assertEquals(18, monster.getHealth());
        assertEquals(18, monster.getMaxHealth());
        assertEquals(6, monster.getAttack());
        assertEquals(1, monster.getDefense());
        assertTrue(monster.isAlive());
    }

    @ParameterizedTest(name = "damage {0} on 10 health -> {1} health, alive={2}")
    @CsvSource({"0,10,true", "1,9,true", "9,1,true", "10,0,false", "11,0,false", "-5,10,true"})
    void takeDamage_boundaries(int damage, int expectedHealth, boolean expectedAlive) {
        Monster monster = new Monster("M", 10, 1, 1, List.of());

        monster.takeDamage(damage);

        assertEquals(expectedHealth, monster.getHealth());
        assertEquals(expectedAlive, monster.isAlive());
        assertEquals(10, monster.getMaxHealth());
    }

    @Test
    void constructor_copiesLoot() {
        List<Item> source = new ArrayList<>();
        source.add(new Key("Key", "d"));
        Monster monster = new Monster("M", 10, 1, 1, source);

        source.add(new Key("Other", "d"));

        assertEquals(1, monster.getLoot().size());
    }

    @Test
    void getLoot_isUnmodifiable() {
        Item key = new Key("Key", "d");
        Monster monster = new Monster("M", 10, 1, 1, List.of(key));

        assertSame(key, monster.getLoot().get(0));
        assertThrows(UnsupportedOperationException.class, () -> monster.getLoot().clear());
    }

    @Test
    void constructor_nullArguments_throw() {
        assertThrows(NullPointerException.class, () -> new Monster(null, 10, 1, 1, List.of()));
        assertThrows(NullPointerException.class, () -> new Monster("M", 10, 1, 1, null));
    }

    @Test
    void zeroHealthMonster_isNotAlive() {
        assertFalse(new Monster("M", 0, 1, 1, List.of()).isAlive());
    }
}
