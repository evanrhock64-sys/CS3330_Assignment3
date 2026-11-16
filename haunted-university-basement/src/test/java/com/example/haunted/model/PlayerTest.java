package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerTest {

    private Inventory inventory;
    private Player player;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(5);
        player = new Player("Hero", 50, 7, 2, inventory);
    }

    @Test
    void constructor_setsInitialState() {
        assertEquals("Hero", player.getName());
        assertEquals(50, player.getHealth());
        assertEquals(50, player.getMaxHealth());
        assertEquals(7, player.getBaseAttack());
        assertEquals(2, player.getBaseDefense());
        assertSame(inventory, player.getInventory());
        assertNull(player.getEquippedWeapon());
        assertNull(player.getEquippedArmor());
        assertNull(player.getCurrentRoom());
        assertTrue(player.isAlive());
    }

    @Test
    void constructor_nullArguments_throw() {
        assertThrows(NullPointerException.class, () -> new Player(null, 50, 7, 2, inventory));
        assertThrows(NullPointerException.class, () -> new Player("Hero", 50, 7, 2, null));
    }

    @ParameterizedTest(name = "damage {0} on 50 health -> {1} health, alive={2}")
    @CsvSource({"0,50,true", "1,49,true", "49,1,true", "50,0,false", "51,0,false", "-10,50,true"})
    void takeDamage_boundaries(int damage, int expectedHealth, boolean expectedAlive) {
        player.takeDamage(damage);

        assertEquals(expectedHealth, player.getHealth());
        assertEquals(expectedAlive, player.isAlive());
    }

    @ParameterizedTest(name = "after 30 damage, heal {0} -> {1} health")
    @CsvSource({"0,20", "1,21", "29,49", "30,50", "31,50", "1000,50", "-5,20"})
    void heal_boundaries(int amount, int expectedHealth) {
        player.takeDamage(30);

        player.heal(amount);

        assertEquals(expectedHealth, player.getHealth());
    }

    @Test
    void heal_deadPlayer_revives() {
        player.takeDamage(50);

        player.heal(10);

        assertTrue(player.isAlive());
        assertEquals(10, player.getHealth());
    }

    @Test
    void powers_withoutEquipment_areBaseValues() {
        assertEquals(7, player.getAttackPower());
        assertEquals(2, player.getDefensePower());
    }

    @Test
    void powers_withEquipment_includeBonuses() {
        Weapon weapon = new Weapon("Stapler", "d", 4);
        Armor armor = new Armor("Shield", "d", 3);

        player.equipWeapon(weapon);
        player.equipArmor(armor);

        assertSame(weapon, player.getEquippedWeapon());
        assertSame(armor, player.getEquippedArmor());
        assertEquals(11, player.getAttackPower());
        assertEquals(5, player.getDefensePower());
    }

    @Test
    void equipWeapon_replacesPrevious() {
        player.equipWeapon(new Weapon("Old", "d", 1));
        player.equipWeapon(new Weapon("New", "d", 6));

        assertEquals("New", player.getEquippedWeapon().getName());
        assertEquals(13, player.getAttackPower());
    }

    @Test
    void equip_null_throws() {
        Weapon weapon = new Weapon("Stapler", "d", 4);
        player.equipWeapon(weapon);

        assertThrows(NullPointerException.class, () -> player.equipWeapon(null));
        assertThrows(NullPointerException.class, () -> player.equipArmor(null));
        assertSame(weapon, player.getEquippedWeapon());
        assertNull(player.getEquippedArmor());
    }

    @Test
    void setCurrentRoom_rejectsNull() {
        Room room = new Room("r", "Room", "d");

        player.setCurrentRoom(room);

        assertSame(room, player.getCurrentRoom());
        assertThrows(NullPointerException.class, () -> player.setCurrentRoom(null));
        assertSame(room, player.getCurrentRoom());
        assertFalse(player.getCurrentRoom() == null);
    }
}
