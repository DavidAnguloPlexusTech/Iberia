package com.plexus.iberia.ui.common

/** The API sends ISO dates such as "2015-07-29T00:00:00Z"; only the day is shown. */
internal fun String.toDisplayDate(): String = substringBefore('T')
