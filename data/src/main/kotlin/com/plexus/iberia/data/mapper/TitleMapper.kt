package com.plexus.iberia.data.mapper

private val TRUNCATION_MARKS = listOf("...", "…")
private const val SENTENCE_END = ". "

internal fun completeTitle(title: String, description: String): String {
    val visiblePart = title.withoutTruncationMark() ?: return title
    if (visiblePart.isEmpty() || !description.startsWith(visiblePart)) return title
    return description.firstSentence(startingAt = visiblePart.length)
}

private fun String.withoutTruncationMark(): String? =
    TRUNCATION_MARKS.firstOrNull { trimEnd().endsWith(it) }
        ?.let { mark -> trimEnd().removeSuffix(mark).trimEnd() }

private fun String.firstSentence(startingAt: Int): String {
    val end = indexOf(SENTENCE_END, startIndex = startingAt)
    val sentence = if (end == -1) this else substring(0, end)
    return sentence.trim().removeSuffix(".")
}
