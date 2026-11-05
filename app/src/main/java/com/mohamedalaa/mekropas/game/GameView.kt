package com.mohamedalaa.mekropas.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

enum class GameState {
    RUNNING,
    GAME_OVER
}

/**
 * Custom SurfaceView providing the main game canvas, dynamic traffic simulation,
 * roadside passenger pickup stations, fare calculation, collision detection, and Game Over state.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, GameEngineProvider {

    private var gameThread: GameThread? = null

    val road = Road()
    val playerMicrobus = PlayerMicrobus()
    val trafficManager = TrafficManager()
    val stationManager = StationManager()

    var gameState = GameState.RUNNING
        private set

    var distanceMeters: Int = 0
        private set

    val totalFareEgp: Int get() = stationManager.totalFareEgp
    val totalPassengers: Int get() = stationManager.totalPassengers
    val routeProgressPercent: Float get() = ((distanceMeters.toFloat() / 3000f) * 100f).coerceIn(0f, 100f)

    private var activeTouchX: Float = -1f
    private var isTouching: Boolean = false
    private var touchDirection: Float = 0f // -1f for left, +1f for right, 0f for static

    // Game Over Overlay Paints
    private val dimOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 15, 15, 20)
    }
    private val dialogBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B71C1C") // Deep Egyptian Alert Red
    }
    private val dialogBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD54F") // Vibrant Gold border
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private val titleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 68f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val subtitleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFECB3")
        textSize = 34f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val restartTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated")
        startThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged: ${width}x$height")
        road.updateSize(width.toFloat(), height.toFloat())
        resetGame()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed")
        stopThread()
    }

    private fun startThread() {
        if (gameThread == null || !gameThread!!.isRunning) {
            gameThread = GameThread(holder, this).apply {
                setRunning(true)
                start()
            }
        }
    }

    private fun stopThread() {
        var retry = true
        gameThread?.setRunning(false)
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                Log.e(TAG, "Error stopping game thread", e)
            }
        }
        gameThread = null
    }

    fun resetGame() {
        gameState = GameState.RUNNING
        distanceMeters = 0
        road.scrollSpeed = 16f
        playerMicrobus.initPosition(
            centerX = road.width / 2f,
            bottomY = road.height
        )
        trafficManager.reset()
        stationManager.reset()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameState == GameState.GAME_OVER) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                performClick()
                resetGame()
            }
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                isTouching = true
                activeTouchX = event.x

                val halfWidth = width / 2f
                touchDirection = if (activeTouchX < halfWidth) {
                    -1f // Touch left half -> steer left
                } else {
                    1f  // Touch right half -> steer right
                }

                playerMicrobus.targetX = activeTouchX.coerceIn(
                    road.roadLeft + playerMicrobus.width / 2f,
                    road.roadRight - playerMicrobus.width / 2f
                )
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    performClick()
                }
                isTouching = false
                touchDirection = 0f
            }
        }
        return true
    }

    override fun updateGame() {
        when (gameState) {
            GameState.RUNNING -> {
                // Update road animation
                road.update()

                // Increment distance
                distanceMeters += (road.scrollSpeed * 0.1f).toInt()

                // Update player steering
                playerMicrobus.steer(
                    direction = if (isTouching) touchDirection else 0f,
                    roadLeft = road.roadLeft,
                    roadRight = road.roadRight
                )

                // Update passenger pickup stations and fare mechanics
                stationManager.update(road, playerMicrobus)

                // Update NPC traffic
                trafficManager.update(road)

                // Collision Detection with NPC traffic
                if (trafficManager.checkCollision(playerMicrobus)) {
                    Log.d(TAG, "Collision detected! Triggering GAME_OVER state.")
                    gameState = GameState.GAME_OVER
                }
            }
            GameState.GAME_OVER -> {
                // Slow down road to a complete stop
                if (road.scrollSpeed > 0f) {
                    road.scrollSpeed = (road.scrollSpeed - 0.8f).coerceAtLeast(0f)
                    road.update()
                }
            }
        }
    }

    override fun drawGame(canvas: Canvas) {
        // Background
        canvas.drawColor(Color.parseColor("#1B5E20"))

        // Render Road
        road.draw(canvas)

        // Render Passenger Pickup Stations
        stationManager.draw(canvas, road)

        // Render NPC Traffic
        trafficManager.draw(canvas)

        // Render Player Microbus
        playerMicrobus.draw(canvas)

        // Render Game Over Overlay if crashed
        if (gameState == GameState.GAME_OVER) {
            drawGameOverOverlay(canvas)
        }
    }

    private fun drawGameOverOverlay(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Semi-transparent dark background
        canvas.drawRect(0f, 0f, w, h, dimOverlayPaint)

        // 2. Center Alert Dialog Box
        val dialogW = w * 0.88f
        val dialogH = h * 0.45f
        val dialogLeft = (w - dialogW) / 2f
        val dialogTop = (h - dialogH) / 2f
        val dialogRect = RectF(dialogLeft, dialogTop, dialogLeft + dialogW, dialogTop + dialogH)

        canvas.drawRoundRect(dialogRect, 28f, 28f, dialogBgPaint)
        canvas.drawRoundRect(dialogRect, 28f, 28f, dialogBorderPaint)

        // 3. Header Text: "حادثة! (Crash!)"
        val centerX = w / 2f
        canvas.drawText("حادثة! (Crash)", centerX, dialogTop + 80f, titleTextPaint)

        // 4. Subtitle
        canvas.drawText("اصطدمت بالمركبة على طريق المنوفية", centerX, dialogTop + 140f, subtitleTextPaint)

        // 5. Game Stats Summary
        val fareText = "الأجرة المجمعة: $totalFareEgp ج.م 💰"
        val passengersText = "إجمالي الركاب: $totalPassengers ركب 👥"
        val distanceText = "المسافة: $distanceMeters متر 🛣️"

        canvas.drawText(fareText, centerX, dialogTop + 200f, subtitleTextPaint)
        canvas.drawText(passengersText, centerX, dialogTop + 250f, subtitleTextPaint)
        canvas.drawText(distanceText, centerX, dialogTop + 300f, subtitleTextPaint)

        // 6. Action prompt: "اضغط على الشاشة لإعادة اللعب"
        val promptBoxRect = RectF(
            dialogLeft + 40f,
            dialogTop + dialogH - 85f,
            dialogLeft + dialogW - 40f,
            dialogTop + dialogH - 20f
        )
        val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#212121")
        }
        canvas.drawRoundRect(promptBoxRect, 16f, 16f, btnPaint)
        canvas.drawText("اضغط لإعادة اللعب 🔄", centerX, dialogTop + dialogH - 42f, restartTextPaint)
    }

    fun pause() {
        stopThread()
    }

    fun resume() {
        startThread()
    }

    companion object {
        private const val TAG = "GameView"
    }
}
