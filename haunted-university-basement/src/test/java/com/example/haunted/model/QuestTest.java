package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestTest {

    private Quest quest;

    @BeforeEach
    void setUp() {
        quest = new Quest("Escape the Basement", "Recover the gradebook.");
    }

    @Test
    void constructor_setsInitialState() {
        assertEquals("Escape the Basement", quest.getName());
        assertEquals("Recover the gradebook.", quest.getDescription());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
        assertFalse(quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());
        assertFalse(quest.isComplete());
    }

    @Test
    void constructor_nullArguments_throw() {
        assertThrows(NullPointerException.class, () -> new Quest(null, "d"));
        assertThrows(NullPointerException.class, () -> new Quest("n", null));
    }

    @Test
    void markGradebookRecovered_setsInProgress() {
        quest.markGradebookRecovered();

        assertTrue(quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertFalse(quest.isComplete());
    }

    @Test
    void markPhantomDefeated_setsInProgress() {
        quest.markPhantomDefeated();

        assertTrue(quest.isPhantomDefeated());
        assertFalse(quest.isGradebookRecovered());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertFalse(quest.isComplete());
    }

    @Test
    void gradebookThenPhantom_completes() {
        quest.markGradebookRecovered();
        quest.markPhantomDefeated();

        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
        assertTrue(quest.isComplete());
    }

    @Test
    void phantomThenGradebook_completes() {
        quest.markPhantomDefeated();
        quest.markGradebookRecovered();

        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
        assertTrue(quest.isComplete());
    }

    @Test
    void markingSameObjectiveTwice_doesNotComplete() {
        quest.markGradebookRecovered();
        quest.markGradebookRecovered();

        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @Test
    void updateStatus_noObjectives_staysNotStarted() {
        quest.updateStatus();

        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }
}
