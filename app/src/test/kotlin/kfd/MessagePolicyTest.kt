package kfd

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MessagePolicyTest {
    @Test
    fun acceptsShortMessage() {
        assertTrue(canSendMessage("Привет"))
    }

    @Test
    fun acceptsNotStandartLengthMessage() {
        assertTrue(canSendMessage("Привет", maxLength = 10))
    }

    @Test
    fun acceptsBlankMessage() {
        assertTrue(canSendMessage(" "))
    }
}