package com.plexus.iberia.data.mapper

private val HTML_TAG = Regex("<[^>]+>")
private val WHITESPACE = Regex("\\s+")
private val HTML_ENTITIES = mapOf(
    "&nbsp;" to " ",
    "&quot;" to "\"",
    "&#39;" to "'",
    "&apos;" to "'",
    "&lt;" to "<",
    "&gt;" to ">",
    "&amp;" to "&",
)

internal fun String.withoutHtml(): String =
    HTML_ENTITIES.entries
        .fold(replace(HTML_TAG, " ")) { text, (entity, character) -> text.replace(entity, character) }
        .replace(WHITESPACE, " ")
        .trim()
