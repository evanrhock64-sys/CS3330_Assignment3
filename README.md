# Group Assignment #3 Haunted University Basement Test Suite

Group Members: Evan & Holdyn

IDE used: Eclipse

Java Version: 21

Test framework: JUnit 5 

## The JUnit tests are in (src/test/java), in the same packages as the code they test:

com.example.haunted.rules: |DamageCalculatorTest|, |TrapResolverTest|, |QuestTrackerTest|

com.example.haunted.model: |PlayerTest|, |InventoryTest|, |RoomTest|, |MonsterTest|, |BossMonsterTest|, |QuestTest|, |TrapTest|, |ItemTest|, |EnumsTest|

com.example.haunted.events: |ResultsTest|

com.example.haunted.engine: |CombatEngineTest|, |MovementEngineTest|, |InteractionEngineTest|, |GameEngineTest|

com.example.haunted.config: |DungeonFactoryTest|

com.example.haunted: |MainTest|

258 tests in total

## How to run tests in Eclipse:

1. Import the project

2. Right-click "src/test/java" -> run as -> JUnit Test
   
3. All 258 tests should pass

## How to get coverage report:

1. Right-click "src/test/java" -> Coverage As -> JUnit Test

## AI use
How Evan Used AI to help:

Prompt: 

Attached are the assignment instructions and the project we were given. My group's task is to write a JUnit 5 test suite for it. I do the JUnit tests and my teammate does the Pitest mutation testing later. We have not started yet. My teacher says AI is allowed for this assignment. Without writing any code, give me a step-by-step plan: Best way to set up the project on my computer. A summary of how the code is organized, class by class. The order I should write the tests in, and why. What kinds of tests to write for each method (normal cases, edge cases, failures, and where parameterized tests fit). How to measure branch coverage with EclEmma as I go. What to hand to my teammate for Pitest, and what the final deliverables are.

AI output:


<img width="729" height="845" alt="image" src="https://github.com/user-attachments/assets/a2f3f927-738a-40c0-a3e5-6169534007d8" />

# Mutation Testing Report
Mutation score: 98% (246/250)
Test strength: 98%
Line coverage of mutated classes: 381/382

Tool: Pitest 1.19.4 + pitest-junit5-plugin 1.2.3 (configured in pom.xml)

How to run: mvn clean test-compile org.pitest:pitest-maven:mutationCoverage
Reports: target/pit-reports/ (HTML, CSV, XML). Copies in PitTest_Report-Baseline/ and PitTest_Report-Final/

Baseline: 246/250 killed (98%)
Final: 249/250 killed (99.6%)

Improvements: 3 mutants in DungeonFactory survived because no test walked those room exits
(Exam Archive -> South, Dean Vault -> West, Dean Vault -> South). Added 3 rows to the
roomsByPath() parameterized test in DungeonFactoryTest to kill them.

Remaining survivor (equivalent mutant): Inventory.addItem line 21. PIT replaces
`return items.add(item)` with `return true`, but ArrayList.add() always returns true,
so no test can tell the difference.

AI Use:

Main prompt (verbatim):

"Here is my assignment 3 my teammate has already done the Junit 5 tests and 
I am doing the mutation tests 
I need help get it set up and 
I will need help actually implementing them" (attached the project zip and the assignment PDF)

Follow-up prompts (summarized):
asked to adapt the setup for IntelliJ, asked whether this matched the assignment, 
asked to keep JUnit 5, asked for step-by-step instructions, 
sent IntelliJ screenshots while fixing the JDK/Maven setup, 
sent the Pitest results and surviving mutants from mutations.csv