package com.mohamedalaa.mekropas.game

import android.graphics.Canvas
import android.util.Log
import android.view.SurfaceHolder

/**
 * High-precision 60 FPS Game Loop Thread for Mekropas.
 * Uses System.nanoTime() with fixed timestep logic to eliminate stutter/jank.
 */
class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameEngine: GameEngineProvider
) : Thread("MekropasGameThread") {

    @Volatile
    var isRunning: Boolean = false
        private set

    private val targetFps = 60
    private val targetFrameTimeNs = 1_000_000_000L / targetFps

    fun setRunning(running: Boolean) {
        isRunning = running
    }

    override fun run() {
        Log.d(TAG, "Game thread started high-precision loop")
        var lastTime = System.nanoTime()
        var accumulator = 0L

        while (isRunning) {
            val now = System.nanoTime()
            var elapsedTime = now - lastTime
            lastTime = now

            // Cap elapsed time to 250ms to prevent spiral of death on lag spikes
            if (elapsedTime > 250_000_000L) {
                elapsedTime = 250_000_000L
            }

            accumulator += elapsedTime

            // Fixed timestep updates
            while (accumulator >= targetFrameTimeNs) {
                gameEngine.updateGame()
                accumulator -= targetFrameTimeNs
            }

            // Render current frame
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        gameEngine.drawGame(canvas)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in game render loop", e)
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error unlocking canvas", e)
                    }
                }
            }

            // High precision frame pacing sleep
            val renderDuration = System.nanoTime() - now
            val sleepTimeNs = targetFrameTimeNs - renderDuration
            if (sleepTimeNs > 2_000_000L) { // Only sleep if more than 2ms remaining
                try {
                    sleep(sleepTimeNs / 1_000_000L, (sleepTimeNs % 1_000_000L).toInt())
                } catch (e: InterruptedException) {
                    // Thread interrupted during sleep
                }
            }
        }
        Log.d(TAG, "Game thread ended loop")
    }

    companion object {
        private const val TAG = "GameThread"
    }
}

/**
 * Interface decoupling GameThread from specific SurfaceView implementation.
 */
interface GameEngineProvider {
    fun updateGame()
    fun drawGame(canvas: Canvas)
}
