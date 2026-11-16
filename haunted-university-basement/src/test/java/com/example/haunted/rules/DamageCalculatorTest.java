package com.example.haunted.rules;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.model.Armor;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Weapon;

class DamageCalculatorTest {

    private final DamageCalculator calculator = new DamageCalculator();

    @ParameterizedTest(name = "player attack {0} vs monster defense {1} -> {2} damage")
    @CsvSource({"7,1,6", "7,0,7", "7,6,1", "7,7,1", "7,8,1", "7,100,1", "1,0,1", "0,0,1"})
    void playerDamage_isAttackMinusDefense_withFloorOfOne(int attack, int defense, int expected) {
        Player player = new Player("Hero", 50, attack, 2, new Inventory(1));
        Monster monster = new Monster("Dummy", 10, 1, defense, List.of());

        assertEquals(expected, calculator.calculatePlayerDamage(player, monster));
    }

    @Test
    void playerDamage_includesEquippedWeaponBonus() {
        Player player = new Player("Hero", 50, 7, 2, new Inventory(1));
        player.equipWeapon(new Weapon("Stapler", "d", 4));
        Monster monster = new Monster("Dummy", 10, 1, 4, List.of());

        assertEquals(7, calculator.calculatePlayerDamage(player, monster));
    }
    
    @ParameterizedTest(name = "monster attack {0} vs player defense {1} -> {2} damage")
    @CsvSource({"6,2,4", "10,0,10", "6,5,1", "6,6,1", "6,7,1", "1,50,1", "0,0,1"})
    void monsterDamage_isAttackMinusDefense_withFloorOfOne(int attack, int defense, int expected) {
        Player player = new Player("Hero", 50, 7, defense, new Inventory(1));
        Monster monster = new Monster("Dummy", 10, attack, 1, List.of());

        assertEquals(expected, calculator.calculateMonsterDamage(monster, player));
    }

    @Test
    void monsterDamage_includesEquippedArmorBonus() {
        Player player = new Player("Hero", 50, 7, 2, new Inventory(1));
        player.equipArmor(new Armor("Shield", "d", 3));
        Monster monster = new Monster("Dummy", 10, 6, 1, List.of());

        assertEquals(1, calculator.calculateMonsterDamage(monster, player));
    }

    @Test
    void monsterDamage_healthyBoss_usesBaseAttack() {
        Player player = new Player("Hero", 50, 7, 2, new Inventory(1));
        BossMonster boss = new BossMonster("Boss", 40, 10, 4, List.of(), 3);

        assertEquals(8, calculator.calculateMonsterDamage(boss, player));
    }

    @Test
    void monsterDamage_enragedBoss_usesBoostedAttack() {
        Player player = new Player("Hero", 50, 7, 2, new Inventory(1));
        BossMonster boss = new BossMonster("Boss", 40, 10, 4, List.of(), 3);
        boss.takeDamage(20);

        assertEquals(11, calculator.calculateMonsterDamage(boss, player));
    }

    @Test
    void monsterDamage_plainMonsterAtLowHealth_hasNoBonus() {
        Player player = new Player("Hero", 50, 7, 2, new Inventory(1));
        Monster monster = new Monster("Plain", 40, 10, 4, List.of());
        monster.takeDamage(30);

        assertEquals(8, calculator.calculateMonsterDamage(monster, player));
    }
}


