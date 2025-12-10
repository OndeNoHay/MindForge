package com.mindforge.core.common.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions for Date
 */

/**
 * Formats date to specified pattern
 */
fun Date.format(pattern: String = "dd/MM/yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

/**
 * Formats date to time string
 */
fun Date.formatTime(pattern: String = "HH:mm:ss"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

/**
 * Formats date to date and time string
 */
fun Date.formatDateTime(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

/**
 * Checks if date is today
 */
fun Date.isToday(): Boolean {
    val today = Date()
    return this.format("dd/MM/yyyy") == today.format("dd/MM/yyyy")
}
