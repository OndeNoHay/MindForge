package com.mindforge.game.core.engine

/**
 * Base interface for game events
 * All game events must implement this interface
 */
interface GameEvent

/**
 * Common game events that apply to all games
 */
sealed class CommonGameEvent : GameEvent {
    /**
     * Start the game
     */
    object Start : CommonGameEvent()

    /**
     * Pause the game
     */
    object Pause : CommonGameEvent()

    /**
     * Resume the game
     */
    object Resume : CommonGameEvent()

    /**
     * Finish the game
     */
    object Finish : CommonGameEvent()

    /**
     * Restart the game
     */
    object Restart : CommonGameEvent()

    /**
     * Tick event for time-based updates
     */
    data class Tick(val deltaTime: Long) : CommonGameEvent()
}
