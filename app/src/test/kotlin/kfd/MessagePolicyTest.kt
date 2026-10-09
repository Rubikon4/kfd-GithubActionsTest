package kfd

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
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

    @Test
    fun respectsDefaultLimit() {
        assertTrue(canSendMessage("a".repeat(140)))
        assertFalse(canSendMessage("a".repeat(141)))
    }

    @Test
    fun acceptsHorizonMessage() {
        assertTrue(canSendMessage("Cat", maxLength = 3))
    }

    @Test
    fun notAcceptedMessage() {
        assertFalse(canSendMessage(null))
    }

    @Test
    fun notAcceptsLongMessage() {
        assertFalse(canSendMessage("Cat", maxLength = 2))
    }

    @Test
    fun rejectsEmptyMessage() {
        assertFalse(canSendMessage("Cat", maxLength = -1))
    }

    @Test
    fun notAcceptsEmptyMessage() {
        assertFalse(canSendMessage(""))
    }
}