package com.mindforge.core.common.utils

/**
 * Utility functions for time operations
 */
object TimeUtils {
    /**
     * Converts milliseconds to minutes
     */
    fun millisToMinutes(millis: Long): Int {
        return (millis / 60000).toInt()
    }

    /**
     * Converts milliseconds to seconds
     */
    fun millisToSeconds(millis: Long): Int {
        return (millis / 1000).toInt()
    }

    /**
     * Formats milliseconds to MM:SS format
     */
    fun formatTime(millis: Long): String {
        val seconds = (millis / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * Formats milliseconds to HH:MM:SS format
     */
    fun formatTimeWithHours(millis: Long): String {
        val seconds = (millis / 1000).toInt()
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }
}
