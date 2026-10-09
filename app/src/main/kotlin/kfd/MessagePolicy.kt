package kfd

fun canSendMessage(
    text: String?,
    maxLength: Int = 140
): Boolean =
    text != null &&
    maxLength > 0 &&
    text.isNotEmpty() &&
    text.length <= maxLength
