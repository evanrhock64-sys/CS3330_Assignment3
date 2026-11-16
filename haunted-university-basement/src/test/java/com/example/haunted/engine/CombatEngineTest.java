package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.haunted.events.CombatResult;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Item;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestStatus;
import com.example.haunted.model.Room;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.DamageCalculator;
import com.example.haunted.rules.QuestTracker;

class CombatEngineTest {

    private Room room;
    private Player player;
    private Quest quest;
    private CombatEngine engine;

    @BeforeEach
    void setUp() {
        room = new Room("r", "Room", "d");
        player = new Player("Hero", 50, 7, 2, new Inventory(5));
        player.setCurrentRoom(room);
        quest = new Quest("Escape", "d");
        engine = new CombatEngine(new DamageCalculator(), new QuestTracker());
    }

    @Test
    void attack_nullMonster_fails() {
        CombatResult result = engine.attack(player, quest, null);

        assertFalse(result.isSuccess());
        assertEquals("Monster not found.", result.getMessage());
        assertEquals(0, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
        assertFalse(result.isMonsterDefeated());
        assertEquals(50, player.getHealth());
    }

    @Test
    void attack_deadPlayer_fails() {
        Monster monster = new Monster("TA", 18, 6, 1, List.of());
        player.takeDamage(50);

        CombatResult result = engine.attack(player, quest, monster);

        assertFalse(result.isSuccess());
        assertEquals("Player is defeated.", result.getMessage());
        assertFalse(result.isMonsterDefeated());
        assertEquals(18, monster.getHealth());
    }

    @Test
    void attack_deadMonster_failsButReportsDefeated() {
        Monster monster = new Monster("TA", 18, 6, 1, List.of(new Weapon("Loot", "d", 1)));
        monster.takeDamage(18);

        CombatResult result = engine.attack(player, quest, monster);

        assertFalse(result.isSuccess());
        assertEquals("Monster is already defeated.", result.getMessage());
        assertTrue(result.isMonsterDefeated());
        assertTrue(result.getDroppedItems().isEmpty());
        assertTrue(room.getItems().isEmpty());
        assertEquals(50, player.getHealth());
    }

    @Test
    void attack_nonLethal_exchangesDamage() {
        Monster monster = new Monster("TA", 18, 6, 1, List.of());

        CombatResult result = engine.attack(player, quest, monster);

        assertTrue(result.isSuccess());
        assertEquals("Attacked TA.", result.getMessage());
        assertEquals(6, result.getDamageToMonster());
        assertEquals(4, result.getDamageToPlayer());
        assertFalse(result.isMonsterDefeated());
        assertTrue(result.getDroppedItems().isEmpty());
        assertEquals(12, monster.getHealth());
        assertEquals(46, player.getHealth());
    }

    @Test
    void attack_exactKill_noCounterattack() {
        Monster monster = new Monster("TA", 6, 6, 1, List.of());

        CombatResult result = engine.attack(player, quest, monster);

        assertTrue(result.isSuccess());
        assertEquals("Defeated TA.", result.getMessage());
        assertEquals(6, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
        assertTrue(result.isMonsterDefeated());
        assertFalse(monster.isAlive());
        assertEquals(50, player.getHealth());
    }

    @Test
    void attack_leavesOneHealth_monsterSurvives() {
        Monster monster = new Monster("TA", 7, 6, 1, List.of());

        CombatResult result = engine.attack(player, quest, monster);

        assertFalse(result.isMonsterDefeated());
        assertEquals(1, monster.getHealth());
        assertEquals(4, result.getDamageToPlayer());
    }

    @Test
    void attack_kill_dropsLootIntoRoom() {
        Item potion = new Potion("Coffee", "d", 15);
        Item sword = new Weapon("Sword", "d", 6);
        Monster monster = new Monster("Golem", 1, 7, 1, List.of(potion, sword));

        CombatResult result = engine.attack(player, quest, monster);

        assertEquals(List.of(potion, sword), result.getDroppedItems());
        assertEquals(2, room.getItems().size());
        assertSame(potion, room.findItem("Coffee").orElseThrow());
        assertSame(sword, room.findItem("Sword").orElseThrow());
    }

    @Test
    void attack_kill_noLoot() {
        Monster monster = new Monster("Ghost", 1, 7, 1, List.of());

        CombatResult result = engine.attack(player, quest, monster);

        assertTrue(result.getDroppedItems().isEmpty());
        assertTrue(room.getItems().isEmpty());
    }

    @Test
    void attack_killPhantom_updatesQuest() {
        Monster phantom = new Monster("Final Exam Phantom", 1, 10, 4, List.of());

        engine.attack(player, quest, phantom);

        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @Test
    void attack_woundPhantom_doesNotUpdateQuest() {
        Monster phantom = new Monster("Final Exam Phantom", 40, 10, 4, List.of());

        engine.attack(player, quest, phantom);

        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    void attack_killOtherMonster_doesNotUpdateQuest() {
        Monster ghost = new Monster("Plagiarism Ghost", 1, 8, 2, List.of());

        engine.attack(player, quest, ghost);

        assertFalse(quest.isPhantomDefeated());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @Test
    void attack_counterattackKillsPlayer() {
        Player weak = new Player("Weak", 3, 7, 2, new Inventory(1));
        weak.setCurrentRoom(room);
        Monster monster = new Monster("Wraith", 30, 10, 3, List.of());

        CombatResult result = engine.attack(weak, quest, monster);

        assertTrue(result.isSuccess());
        assertEquals(8, result.getDamageToPlayer());
        assertEquals(0, weak.getHealth());
        assertFalse(weak.isAlive());
    }

    @Test
    void attack_healthyBoss_usesBaseAttack() {
        BossMonster boss = new BossMonster("Boss", 40, 10, 4, List.of(), 3);

        CombatResult result = engine.attack(player, quest, boss);

        assertEquals(3, result.getDamageToMonster());
        assertEquals(8, result.getDamageToPlayer());
    }

    @Test
    void attack_boss_becomesEnragedMidFight() {
        BossMonster boss = new BossMonster("Boss", 40, 10, 4, List.of(), 3);
        boss.takeDamage(17);

        CombatResult result = engine.attack(player, quest, boss);

        assertEquals(20, boss.getHealth());
        assertEquals(11, result.getDamageToPlayer());
    }
}
