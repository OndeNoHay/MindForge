package com.mindforge.game.core.engine

import kotlinx.coroutines.flow.StateFlow

/**
 * Core game engine interface
 * All games must implement this interface
 *
 * @param State The type of game state
 * @param Event The type of game events
 * @param Result The type of game result
 */
interface GameEngine<State : GameState, Event : GameEvent, Result : GameResult> {
    /**
     * Current state of the game as a StateFlow
     */
    val currentState: StateFlow<State>

    /**
     * Process a game event
     *
     * @param event The event to process
     */
    fun processEvent(event: Event)

    /**
     * Start the game
     */
    fun start()

    /**
     * Pause the game
     */
    fun pause()

    /**
     * Resume the game
     */
    fun resume()

    /**
     * Finish the game and return the result
     *
     * @return The game result
     */
    fun finish(): Result

    /**
     * Restart the game
     */
    fun restart()
}

/**
 * Abstract base implementation of GameEngine
 * Provides common functionality for all games
 */
abstract class BaseGameEngine<State : GameState, Event : GameEvent, Result : GameResult> :
    GameEngine<State, Event, Result> {

    protected abstract fun onStart()
    protected abstract fun onPause()
    protected abstract fun onResume()
    protected abstract fun onFinish(): Result
    protected abstract fun onRestart()
    protected abstract fun onProcessEvent(event: Event)

    override fun start() {
        if (currentState.value.phase == GamePhase.READY) {
            onStart()
        }
    }

    override fun pause() {
        if (currentState.value.phase == GamePhase.PLAYING) {
            onPause()
        }
    }

    override fun resume() {
        if (currentState.value.phase == GamePhase.PAUSED) {
            onResume()
        }
    }

    override fun finish(): Result {
        return onFinish()
    }

    override fun restart() {
        onRestart()
    }

    override fun processEvent(event: Event) {
        onProcessEvent(event)
    }
}
