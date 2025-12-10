package com.mindforge.game.core.engine

/**
 * Represents the different phases a game can be in
 */
enum class GamePhase {
    /**
     * Game is ready to start but hasn't begun yet
     */
    READY,

    /**
     * Game is currently being played
     */
    PLAYING,

    /**
     * Game is paused
     */
    PAUSED,

    /**
     * Game has been completed
     */
    COMPLETED,

    /**
     * Review phase - showing results or feedback
     */
    REVIEW
}
