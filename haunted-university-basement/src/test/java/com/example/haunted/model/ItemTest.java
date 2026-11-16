package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ItemTest {

    static Stream<Arguments> allItemTypes() {
        return Stream.of(
                Arguments.of(new Key("Archive Key", "Opens the archive."), "Archive Key", "Opens the archive."),
                Arguments.of(new QuestItem("Lost Gradebook", "Missing."), "Lost Gradebook", "Missing."),
                Arguments.of(new Weapon("Stapler", "Heavy.", 4), "Stapler", "Heavy."),
                Arguments.of(new Armor("Shield", "Battered.", 3), "Shield", "Battered."),
                Arguments.of(new Potion("Coffee", "Warm.", 15), "Coffee", "Warm."));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("allItemTypes")
    void item_exposesNameDescriptionAndToString(Item item, String name, String description) {
        assertEquals(name, item.getName());
        assertEquals(description, item.getDescription());
        assertEquals(name, item.toString());
    }

    static Stream<Arguments> nullArguments() {
        return Stream.of(Arguments.of(null, "desc"), Arguments.of("name", null), Arguments.of(null, null));
    }

    @ParameterizedTest
    @MethodSource("nullArguments")
    void item_nullArguments_throw(String name, String description) {
        assertThrows(NullPointerException.class, () -> new Key(name, description));
    }

    @Test
    void itemTypes_implementTheirInterfaces() {
        assertInstanceOf(Equippable.class, new Weapon("W", "d", 1));
        assertInstanceOf(Equippable.class, new Armor("A", "d", 1));
        assertInstanceOf(Usable.class, new Potion("P", "d", 1));
    }

    @Test
    void itemBonuses_areExposed() {
        assertEquals(4, new Weapon("W", "d", 4).getAttackBonus());
        assertEquals(3, new Armor("A", "d", 3).getDefenseBonus());
        assertEquals(15, new Potion("P", "d", 15).getHealingAmount());
    }

    @Test
    void potion_use_healsPlayer() {
        Player player = new Player("Hero", 50, 7, 2, new Inventory(1));
        player.takeDamage(30);

        new Potion("P", "d", 15).use(player);

        assertEquals(35, player.getHealth());
    }
}
