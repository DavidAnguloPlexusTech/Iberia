package com.plexus.iberia.data.mapper

private val TRUNCATION_MARKS = listOf("...", "…")
private const val SENTENCE_END = ". "

/**
 * The API cuts some long titles, such as "One of NASA Glenn's T-34C research aircraft is removed from t...",
 * and the full sentence is the beginning of the description. Restores it so no information is lost;
 * any other title is returned unchanged.
 */
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
