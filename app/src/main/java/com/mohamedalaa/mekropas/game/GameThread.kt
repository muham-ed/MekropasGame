package com.mohamedalaa.mekropas.game

import android.graphics.Canvas
import android.util.Log
import android.view.SurfaceHolder

/**
 * High-performance 60 FPS Game Loop Thread for Mekropas.
 * Handles game state updates and Surface Canvas rendering safely.
 */
class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameEngine: GameEngineProvider
) : Thread("MekropasGameThread") {

    @Volatile
    var isRunning: Boolean = false
        private set

    private val targetFps = 60
    private val targetFrameTimeMs = 1000L / targetFps

    fun setRunning(running: Boolean) {
        isRunning = running
    }

    override fun run() {
        Log.d(TAG, "Game thread started loop")
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long

        while (isRunning) {
            startTime = System.currentTimeMillis()
            var canvas: Canvas? = null

            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        gameEngine.updateGame()
                        gameEngine.drawGame(canvas)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in game loop", e)
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error unlocking canvas", e)
                    }
                }
            }

            timeMillis = System.currentTimeMillis() - startTime
            waitTime = targetFrameTimeMs - timeMillis

            if (waitTime > 0) {
                try {
                    sleep(waitTime)
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
