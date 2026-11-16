package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Armor;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Item;
import com.example.haunted.model.Key;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.QuestStatus;
import com.example.haunted.model.Room;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.QuestTracker;

class InteractionEngineTest {

    private Room room;
    private Player player;
    private Quest quest;
    private InteractionEngine engine;

    @BeforeEach
    void setUp() {
        room = new Room("r", "Room", "d");
        player = new Player("Hero", 50, 7, 2, new Inventory(2));
        player.setCurrentRoom(room);
        quest = new Quest("Escape", "d");
        engine = new InteractionEngine(new QuestTracker());
    }

    @Nested
    @DisplayName("pickUpItem")
    class PickUp {

        @Test
        void pickUp_success() {
            Item key = new Key("Archive Key", "d");
            room.addItem(key);

            InteractionResult result = engine.pickUpItem(player, quest, "Archive Key");

            assertTrue(result.isSuccess());
            assertEquals("Picked up Archive Key.", result.getMessage());
            assertTrue(room.getItems().isEmpty());
            assertSame(key, player.getInventory().findItem("Archive Key").orElseThrow());
        }

        @ParameterizedTest(name = "typed as \"{0}\"")
        @ValueSource(strings = {"archive key", "ARCHIVE KEY"})
        void pickUp_caseInsensitive(String typed) {
            room.addItem(new Key("Archive Key", "d"));

            InteractionResult result = engine.pickUpItem(player, quest, typed);

            assertTrue(result.isSuccess());
            assertEquals("Picked up Archive Key.", result.getMessage());
        }

        @Test
        void pickUp_missing() {
            InteractionResult result = engine.pickUpItem(player, quest, "Ghost Key");

            assertFalse(result.isSuccess());
            assertEquals("Item not found in the room.", result.getMessage());
        }

        @Test
        void pickUp_fullInventory_returnsItemToRoom() {
            player.getInventory().addItem(new Key("A", "d"));
            player.getInventory().addItem(new Key("B", "d"));
            Item extra = new Key("Extra", "d");
            room.addItem(extra);

            InteractionResult result = engine.pickUpItem(player, quest, "Extra");

            assertFalse(result.isSuccess());
            assertEquals("Inventory is full.", result.getMessage());
            assertSame(extra, room.findItem("Extra").orElseThrow());
            assertFalse(player.getInventory().contains("Extra"));
            assertEquals(2, player.getInventory().getItems().size());
        }

        @Test
        void pickUp_gradebook_updatesQuest() {
            room.addItem(new QuestItem("Lost Gradebook", "d"));

            engine.pickUpItem(player, quest, "Lost Gradebook");

            assertTrue(quest.isGradebookRecovered());
            assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        }

        @Test
        void pickUp_otherItem_doesNotUpdateQuest() {
            room.addItem(new Key("Archive Key", "d"));

            engine.pickUpItem(player, quest, "Archive Key");

            assertFalse(quest.isGradebookRecovered());
        }

        @Test
        void pickUp_gradebookRefused_doesNotUpdateQuest() {
            player.getInventory().addItem(new Key("A", "d"));
            player.getInventory().addItem(new Key("B", "d"));
            room.addItem(new QuestItem("Lost Gradebook", "d"));

            engine.pickUpItem(player, quest, "Lost Gradebook");

            assertFalse(quest.isGradebookRecovered());
        }
    }

    @Nested
    class Use {

        @Test
        void use_potion() {
            player.takeDamage(30);
            player.getInventory().addItem(new Potion("Coffee", "d", 15));

            InteractionResult result = engine.useItem(player, "Coffee");

            assertTrue(result.isSuccess());
            assertEquals("Used Coffee.", result.getMessage());
            assertEquals(35, player.getHealth());
            assertFalse(player.getInventory().contains("Coffee"));
        }

        @Test
        void use_missing() {
            InteractionResult result = engine.useItem(player, "Coffee");

            assertFalse(result.isSuccess());
            assertEquals("Item not found in inventory.", result.getMessage());
        }

        @Test
        void use_notUsable() {
            player.getInventory().addItem(new Key("Archive Key", "d"));

            InteractionResult result = engine.useItem(player, "Archive Key");

            assertFalse(result.isSuccess());
            assertEquals("That item cannot be used.", result.getMessage());
            assertTrue(player.getInventory().contains("Archive Key"));
        }

        @Test
        void use_itemOnlyInRoom() {
            room.addItem(new Potion("Coffee", "d", 15));

            assertFalse(engine.useItem(player, "Coffee").isSuccess());
        }
    }

    @Nested
    class Equip {

        @Test
        void equip_weapon() {
            Weapon weapon = new Weapon("Stapler", "d", 4);
            player.getInventory().addItem(weapon);

            InteractionResult result = engine.equipItem(player, "Stapler");

            assertTrue(result.isSuccess());
            assertEquals("Equipped weapon Stapler.", result.getMessage());
            assertSame(weapon, player.getEquippedWeapon());
        }

        @Test
        void equip_armor() {
            Armor armor = new Armor("Shield", "d", 3);
            player.getInventory().addItem(armor);

            InteractionResult result = engine.equipItem(player, "Shield");

            assertTrue(result.isSuccess());
            assertEquals("Equipped armor Shield.", result.getMessage());
            assertSame(armor, player.getEquippedArmor());
        }

        @Test
        void equip_missing() {
            InteractionResult result = engine.equipItem(player, "Stapler");

            assertFalse(result.isSuccess());
            assertEquals("Item not found in inventory.", result.getMessage());
        }

        @ParameterizedTest(name = "item type {0}")
        @ValueSource(strings = {"key", "potion", "quest"})
        void equip_notEquippable(String type) {
            Item item = switch (type) {
                case "key" -> new Key("Thing", "d");
                case "potion" -> new Potion("Thing", "d", 5);
                default -> new QuestItem("Thing", "d");
            };
            player.getInventory().addItem(item);

            InteractionResult result = engine.equipItem(player, "Thing");

            assertFalse(result.isSuccess());
            assertEquals("That item cannot be equipped.", result.getMessage());
        }

        @Test
        void equip_keepsItemInInventory() {
            player.getInventory().addItem(new Weapon("Stapler", "d", 4));

            engine.equipItem(player, "Stapler");

            assertTrue(player.getInventory().contains("Stapler"));
        }
    }

    @Nested
    class Unlock {

        private Room target;

        @BeforeEach
        void connectTarget() {
            target = new Room("t", "Archive", "d");
            room.connect(Direction.NORTH, target);
        }

        @Test
        void unlock_noRoom() {
            InteractionResult result = engine.unlockRoom(player, Direction.SOUTH);

            assertFalse(result.isSuccess());
            assertEquals("There is no room in that direction.", result.getMessage());
        }

        @Test
        void unlock_alreadyUnlocked() {
            InteractionResult result = engine.unlockRoom(player, Direction.NORTH);

            assertTrue(result.isSuccess());
            assertEquals("The room is already unlocked.", result.getMessage());
        }

        @Test
        void unlock_noRequiredKey() {
            target.setLocked(true, null);

            InteractionResult result = engine.unlockRoom(player, Direction.NORTH);

            assertFalse(result.isSuccess());
            assertEquals("The room cannot be unlocked.", result.getMessage());
            assertTrue(target.isLocked());
        }

        @Test
        void unlock_missingKey() {
            target.setLocked(true, "Archive Key");

            InteractionResult result = engine.unlockRoom(player, Direction.NORTH);

            assertFalse(result.isSuccess());
            assertEquals("You do not have the correct key.", result.getMessage());
            assertTrue(target.isLocked());
        }

        @Test
        void unlock_wrongKey() {
            target.setLocked(true, "Archive Key");
            player.getInventory().addItem(new Key("Vault Key", "d"));

            InteractionResult result = engine.unlockRoom(player, Direction.NORTH);

            assertFalse(result.isSuccess());
            assertTrue(target.isLocked());
        }

        @Test
        void unlock_rightNameButNotAKey() {
            target.setLocked(true, "Archive Key");
            player.getInventory().addItem(new Weapon("Archive Key", "d", 1));

            InteractionResult result = engine.unlockRoom(player, Direction.NORTH);

            assertFalse(result.isSuccess());
            assertEquals("You do not have the correct key.", result.getMessage());
            assertTrue(target.isLocked());
        }

        @Test
        void unlock_success() {
            target.setLocked(true, "Archive Key");
            player.getInventory().addItem(new Key("archive key", "d"));

            InteractionResult result = engine.unlockRoom(player, Direction.NORTH);

            assertTrue(result.isSuccess());
            assertEquals("Unlocked Archive.", result.getMessage());
            assertFalse(target.isLocked());
            assertTrue(player.getInventory().contains("Archive Key"));
        }
    }
}
