package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.config.DungeonFactory;
import com.example.haunted.events.CombatResult;
import com.example.haunted.events.InteractionResult;
import com.example.haunted.events.MoveResult;
import com.example.haunted.model.Direction;
import com.example.haunted.model.QuestStatus;

class GameEngineTest {

    private GameEngine game;

    @BeforeEach
    void setUp() {
        game = DungeonFactory.createGame();
    }

    private void move(Direction direction) {
        assertTrue(game.move(direction).isSuccess(), "move " + direction);
    }

    private void pickUp(String item) {
        assertTrue(game.pickUpItem(item).isSuccess(), "pick up " + item);
    }

    private CombatResult attackUntilDefeated(String monster) {
        CombatResult last = null;
        for (int i = 0; i < 50 && (last == null || !last.isMonsterDefeated()); i++) {
            last = game.attack(monster);
            assertTrue(last.isSuccess(), "attack " + monster);
        }
        assertTrue(last.isMonsterDefeated(), monster + " should be defeated");
        return last;
    }

    @Test
    void newGame_initialState() {
        assertEquals("Maintenance Stairwell", game.getCurrentRoom().getName());
        assertSame(game.getPlayer().getCurrentRoom(), game.getCurrentRoom());
        assertEquals(QuestStatus.NOT_STARTED, game.getQuest().getStatus());
        assertFalse(game.isGameOver());
        assertFalse(game.isGameWon());
    }

    @Test
    void move_throughGameEngine() {
        MoveResult west = game.move(Direction.WEST);
        MoveResult east = game.move(Direction.EAST);

        assertFalse(west.isSuccess());
        assertTrue(east.isSuccess());
        assertEquals("Abandoned Lecture Hall", game.getCurrentRoom().getName());
    }

    @Test
    void itemHandling_throughGameEngine() {
        move(Direction.EAST);
        game.getPlayer().takeDamage(20);

        assertTrue(game.pickUpItem("Coffee Potion").isSuccess());
        assertTrue(game.useItem("Coffee Potion").isSuccess());
        assertEquals(45, game.getPlayer().getHealth());

        move(Direction.EAST);
        pickUp("Calculator Shield");
        InteractionResult equip = game.equipItem("Calculator Shield");
        assertTrue(equip.isSuccess());
        assertEquals(5, game.getPlayer().getDefensePower());
    }

    @Test
    void attack_missingMonster_fails() {
        CombatResult result = game.attack("Final Exam Phantom");

        assertFalse(result.isSuccess());
        assertEquals("Monster not found.", result.getMessage());
    }

    @Test
    void attack_caseInsensitive() {
        move(Direction.EAST);

        CombatResult result = game.attack("sleep-deprived ta");

        assertTrue(result.isSuccess());
        assertEquals("Attacked Sleep-Deprived TA.", result.getMessage());
    }

    @Test
    void lockedRoom_requiresKey() {
        move(Direction.EAST);
        assertFalse(game.move(Direction.NORTH).isSuccess());
        assertFalse(game.unlockRoom(Direction.NORTH).isSuccess());

        move(Direction.EAST);
        pickUp("Archive Key");
        move(Direction.WEST);

        assertTrue(game.unlockRoom(Direction.NORTH).isSuccess());
        assertTrue(game.move(Direction.NORTH).isSuccess());
        assertEquals("Exam Archive", game.getCurrentRoom().getName());
    }

    @Test
    void brokenElevator_trapFiresOnce() {
        move(Direction.EAST);

        MoveResult first = game.move(Direction.SOUTH);
        move(Direction.NORTH);
        MoveResult second = game.move(Direction.SOUTH);

        assertTrue(first.isTrapTriggered());
        assertEquals(8, first.getTrapDamage());
        assertFalse(second.isTrapTriggered());
        assertEquals(42, game.getPlayer().getHealth());
    }

    @Test
    void isGameOver_whenPlayerDead() {
        game.getPlayer().takeDamage(49);
        assertFalse(game.isGameOver());

        game.getPlayer().takeDamage(1);
        assertTrue(game.isGameOver());
    }

    @Test
    void isGameWon_requiresAlivePlayer() {
        game.getQuest().markGradebookRecovered();
        game.getQuest().markPhantomDefeated();
        assertTrue(game.isGameWon());

        game.getPlayer().takeDamage(50);

        assertFalse(game.isGameWon());
        assertTrue(game.isGameOver());
    }

    @ParameterizedTest(name = "gradebook={0}, phantom={1} -> won={2}")
    @CsvSource({"false,false,false", "true,false,false", "false,true,false", "true,true,true"})
    void isGameWon_requiresBothObjectives(boolean gradebook, boolean phantom, boolean expectedWon) {
        if (gradebook) {
            game.getQuest().markGradebookRecovered();
        }
        if (phantom) {
            game.getQuest().markPhantomDefeated();
        }

        assertEquals(expectedWon, game.isGameWon());
    }

    @Test
    void fullPlaythrough_winsTheGame() {
        // Lecture hall: kill the TA, then head to the lab storage.
        move(Direction.EAST);
        attackUntilDefeated("Sleep-Deprived TA");
        move(Direction.EAST);
        pickUp("Archive Key");
        pickUp("Calculator Shield");
        assertTrue(game.equipItem("Calculator Shield").isSuccess());

        // Server closet: grab the stapler and equip it.
        move(Direction.NORTH);
        pickUp("Stapler of Justice");
        assertTrue(game.equipItem("Stapler of Justice").isSuccess());
        move(Direction.SOUTH);
        move(Direction.WEST);

        // Exam archive: unlock, take the gradebook.
        assertTrue(game.unlockRoom(Direction.NORTH).isSuccess());
        move(Direction.NORTH);
        pickUp("Lost Gradebook");
        assertEquals(QuestStatus.IN_PROGRESS, game.getQuest().getStatus());
        assertFalse(game.isGameWon());

        // Dean vault: get the vault key, unlock the final chamber.
        move(Direction.EAST);
        pickUp("Vault Key");
        assertTrue(game.unlockRoom(Direction.NORTH).isSuccess());
        move(Direction.NORTH);
        assertEquals("Final Chamber", game.getCurrentRoom().getName());

        // Final chamber: defeat the boss.
        assertFalse(game.isGameWon());
        CombatResult finalBlow = attackUntilDefeated("Final Exam Phantom");

        assertTrue(finalBlow.isMonsterDefeated());
        assertEquals(QuestStatus.COMPLETED, game.getQuest().getStatus());
        assertTrue(game.getPlayer().isAlive());
        assertTrue(game.isGameWon());
        assertFalse(game.isGameOver());
    }

    @Test
    void unpreparedBossFight_playerDies() {
        game.getQuest().markGradebookRecovered();
        move(Direction.EAST);
        move(Direction.EAST);
        pickUp("Archive Key");
        move(Direction.WEST);
        game.unlockRoom(Direction.NORTH);
        move(Direction.NORTH);
        move(Direction.EAST);
        pickUp("Vault Key");
        game.unlockRoom(Direction.NORTH);
        move(Direction.NORTH);

        for (int i = 0; i < 100 && game.getPlayer().isAlive(); i++) {
            game.attack("Final Exam Phantom");
        }

        assertTrue(game.isGameOver());
        assertFalse(game.isGameWon());
        assertFalse(game.getQuest().isPhantomDefeated());
        assertFalse(game.attack("Final Exam Phantom").isSuccess());
    }
}
