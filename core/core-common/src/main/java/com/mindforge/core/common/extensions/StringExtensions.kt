package com.mindforge.core.common.extensions

/**
 * Extension functions for String
 */

/**
 * Capitalizes first letter of the string
 */
fun String.capitalizeFirst(): String {
    return if (this.isNotEmpty()) {
        this[0].uppercase() + this.substring(1)
    } else {
        this
    }
}

/**
 * Truncates string to specified length and adds ellipsis if needed
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength - ellipsis.length) + ellipsis
    } else {
        this
    }
}

/**
 * Checks if string is a valid email
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
