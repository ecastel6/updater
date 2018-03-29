package app.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionTest
{

    @Test
    void compareTo() {
        assertTrue(new Version("1.0").equals(new Version("1")));
        assertEquals(new Version("2.1.0"), new Version("2.1"));
        assertEquals(1, new Version("3.12.2").compareTo(new Version("3.1.3")));
        assertEquals(-1, new Version("3.0.2").compareTo(new Version("3.1.3")));
        assertEquals(0, new Version("3.2").compareTo(new Version("3.2.0")));
    }

    @Test
    void equals() {
    }
}