package kfd

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DisplayNameTest {
    @Test
    fun preservesName() {
        assertEquals("Анна", displayName("Анна"))
    }

    @Test
    fun handlesNull() {
        assertEquals("Гость", displayName(null))
    }

    @Test
    fun handlesEmpty() {
        assertEquals("Гость", displayName(""))
    }

    @Test
    fun handlesEmptySpace() {
        assertEquals("Гость", displayName(" "))
    }

    @Test
    fun handleTrim() {
        assertEquals("Анна", displayName(" Анна "))
    }
}
