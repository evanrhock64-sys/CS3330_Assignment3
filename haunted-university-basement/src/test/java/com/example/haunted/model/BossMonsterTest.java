package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossMonsterTest {

    @ParameterizedTest(name = "max health {0}, damage taken {1} -> current attack {2}")
    @CsvSource({
        "40,0,10",
        "40,19,10",
        "40,20,13",
        "40,21,13",
        "40,40,13",
        "41,20,10",
        "41,21,13",
        "1,0,10",
        "1,1,13"
    })
    void getCurrentAttack_enragesAtHalfHealth(int maxHealth, int damageTaken, int expectedAttack) {
        BossMonster boss = new BossMonster("Boss", maxHealth, 10, 4, List.of(), 3);
        boss.takeDamage(damageTaken);

        assertEquals(expectedAttack, boss.getCurrentAttack());
    }

    @Test
    void getAttack_isNotAffectedByRage() {
        BossMonster boss = new BossMonster("Boss", 40, 10, 4, List.of(), 3);
        boss.takeDamage(30);

        assertEquals(10, boss.getAttack());
    }
}
