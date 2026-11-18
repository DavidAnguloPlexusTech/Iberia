package com.plexus.iberia.ui.common

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

private const val API_DATE_PATTERN = "yyyy-MM-dd"

/** The API sends ISO dates such as "2015-07-29T00:00:00Z"; only the day is shown. */
internal fun String.toDisplayDate(): String = substringBefore('T')

/** Date as screen readers should say it, such as "July 29, 2015"; falls back to the displayed date. */
internal fun String.toAccessibleDate(): String {
    val displayDate = toDisplayDate()
    return try {
        val date = SimpleDateFormat(API_DATE_PATTERN, Locale.US).parse(displayDate)
        if (date == null) displayDate else DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH).format(date)
    } catch (exception: ParseException) {
        displayDate
    }
}
