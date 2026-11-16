package com.example.haunted.rules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.haunted.model.Key;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.QuestStatus;

class QuestTrackerTest {

    private final QuestTracker tracker = new QuestTracker();
    private Quest quest;

    @BeforeEach
    void setUp() {
        quest = new Quest("Escape", "Get out");
    }

    @ParameterizedTest(name = "item name \"{0}\" recovers the gradebook")
    @ValueSource(strings = {"Lost Gradebook", "lost gradebook", "LOST GRADEBOOK"})
    void updateQuestForItem_gradebook_marksRecovered(String name) {
        tracker.updateQuestForItem(quest, new QuestItem(name, "d"));

        assertTrue(quest.isGradebookRecovered());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @ParameterizedTest(name = "item name \"{0}\" does not recover the gradebook")
    @ValueSource(strings = {"Gradebook", "Lost Gradebook 2", " Lost Gradebook", "Archive Key", ""})
    void updateQuestForItem_otherItem_doesNothing(String name) {
        tracker.updateQuestForItem(quest, new Key(name, "d"));

        assertFalse(quest.isGradebookRecovered());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @Test
    void updateQuestForItem_nullItem_isIgnored() {
        assertDoesNotThrow(() -> tracker.updateQuestForItem(quest, null));
        assertFalse(quest.isGradebookRecovered());
    }

    @ParameterizedTest(name = "dead monster named \"{0}\" defeats the phantom")
    @ValueSource(strings = {"Final Exam Phantom", "final exam phantom", "FINAL EXAM PHANTOM"})
    void updateQuestForMonster_deadPhantom_marksDefeated(String name) {
        Monster phantom = new Monster(name, 10, 1, 1, List.of());
        phantom.takeDamage(10);

        tracker.updateQuestForMonster(quest, phantom);

        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @Test
    void updateQuestForMonster_livingPhantom_doesNothing() {
        Monster phantom = new Monster("Final Exam Phantom", 10, 1, 1, List.of());
        phantom.takeDamage(9);

        tracker.updateQuestForMonster(quest, phantom);

        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    void updateQuestForMonster_otherDeadMonster_doesNothing() {
        Monster ghost = new Monster("Plagiarism Ghost", 10, 1, 1, List.of());
        ghost.takeDamage(10);

        tracker.updateQuestForMonster(quest, ghost);

        assertFalse(quest.isPhantomDefeated());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
    }

    @Test
    void updateQuestForMonster_nullMonster_isIgnored() {
        assertDoesNotThrow(() -> tracker.updateQuestForMonster(quest, null));
        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    void bothObjectives_completeQuest() {
        Monster phantom = new Monster("Final Exam Phantom", 10, 1, 1, List.of());
        phantom.takeDamage(10);

        tracker.updateQuestForItem(quest, new QuestItem("Lost Gradebook", "d"));
        tracker.updateQuestForMonster(quest, phantom);

        assertTrue(quest.isComplete());
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    }
}
