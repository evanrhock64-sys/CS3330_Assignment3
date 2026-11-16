package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class InventoryTest {

    private Item item(String name) {
        return new Key(name, "d");
    }

    @ParameterizedTest(name = "capacity {0}, items added {1} -> full is {2}")
    @CsvSource({"0,0,true", "1,0,false", "1,1,true", "3,2,false", "3,3,true"})
    void isFull_boundaries(int capacity, int itemsAdded, boolean expectedFull) {
        Inventory inventory = new Inventory(capacity);
        for (int i = 0; i < itemsAdded; i++) {
            assertTrue(inventory.addItem(item("item" + i)));
        }

        assertEquals(expectedFull, inventory.isFull());
        assertEquals(capacity, inventory.getCapacity());
    }

    @Test
    void addItem_whenFull_returnsFalse() {
        Inventory inventory = new Inventory(1);
        inventory.addItem(item("first"));

        assertFalse(inventory.addItem(item("second")));
        assertEquals(1, inventory.getItems().size());
        assertFalse(inventory.contains("second"));
    }

    @ParameterizedTest(name = "searching for \"{0}\" finds the Archive Key")
    @ValueSource(strings = {"Archive Key", "archive key", "ARCHIVE KEY"})
    void findItem_isCaseInsensitive(String query) {
        Inventory inventory = new Inventory(2);
        Item key = item("Archive Key");
        inventory.addItem(key);

        assertSame(key, inventory.findItem(query).orElseThrow());
        assertTrue(inventory.contains(query));
    }

    @Test
    void findItem_missing_isEmpty() {
        Inventory inventory = new Inventory(2);
        inventory.addItem(item("Archive Key"));

        assertTrue(inventory.findItem("Vault Key").isEmpty());
        assertFalse(inventory.contains("Vault Key"));
    }

    @Test
    void removeItem_existing_returnsItemAndFreesSlot() {
        Inventory inventory = new Inventory(1);
        Item key = item("Archive Key");
        inventory.addItem(key);

        assertSame(key, inventory.removeItem("archive key"));
        assertFalse(inventory.contains("Archive Key"));
        assertFalse(inventory.isFull());
    }

    @Test
    void removeItem_missing_returnsNull() {
        Inventory inventory = new Inventory(2);
        inventory.addItem(item("Archive Key"));

        assertNull(inventory.removeItem("Vault Key"));
        assertEquals(1, inventory.getItems().size());
    }

    @Test
    void removeItem_duplicateNames_removesOnlyFirst() {
        Inventory inventory = new Inventory(3);
        Item first = item("Key");
        Item second = item("Key");
        inventory.addItem(first);
        inventory.addItem(second);

        assertSame(first, inventory.removeItem("Key"));
        assertSame(second, inventory.getItems().get(0));
    }

    @Test
    void getItems_isUnmodifiable() {
        Inventory inventory = new Inventory(2);
        inventory.addItem(item("Key"));

        assertThrows(UnsupportedOperationException.class, () -> inventory.getItems().add(item("Other")));
        assertThrows(UnsupportedOperationException.class, () -> inventory.getItems().remove(0));
    }
}
