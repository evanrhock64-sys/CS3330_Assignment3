package com.example.haunted;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void main_printsDemoOutput() {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
        } finally {
            System.setOut(original);
        }

        String[] lines = buffer.toString(StandardCharsets.UTF_8).split("\\R");
        assertEquals(3, lines.length);
        assertEquals("Maintenance Stairwell", lines[0]);
        assertEquals("Moved to Abandoned Lecture Hall.", lines[1]);
        assertEquals("Picked up Coffee Potion.", lines[2]);
    }
}
