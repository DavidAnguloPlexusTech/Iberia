package com.plexus.iberia.ui.common

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

private const val API_DATE_PATTERN = "yyyy-MM-dd"

internal fun String.toDisplayDate(): String = substringBefore('T')

internal fun String.toAccessibleDate(): String {
    val displayDate = toDisplayDate()
    return try {
        val date = SimpleDateFormat(API_DATE_PATTERN, Locale.US).parse(displayDate)
        if (date == null) displayDate else DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH).format(date)
    } catch (exception: ParseException) {
        displayDate
    }
}
