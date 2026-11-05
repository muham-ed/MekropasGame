package com.mohamedalaa.mekropas.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * Represents a roadside passenger pickup station in Menofia (e.g. موقف الشهداء, قرية طملاي).
 */
class PassengerStation(
    val name: String,
    val x: Float,
    var y: Float,
    val commuterCount: Int = 3,
    val farePerCommuter: Int = 5
) {
    var isCollected: Boolean = false
        private set

    val totalFare: Int get() = commuterCount * farePerCommuter

    // Paints
    private val shelterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1565C0") // Blue station canopy
    }
    private val signBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFF8E1") // Off-white sign
    }
    private val signBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6F00") // Amber border
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#212121")
        textSize = 18f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val pickupZonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 76, 175, 80) // Translucent green pickup highlight
        style = Paint.Style.FILL
    }
    private val pickupZoneBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val commuterHeadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
    }
    private val commuterShirtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E65100")
    }

    fun update(roadScrollSpeed: Float) {
        y += roadScrollSpeed
    }

    fun collect() {
        isCollected = true
    }

    fun draw(canvas: Canvas, roadRight: Float, laneWidth: Float) {
        val stationW = 100f
        val stationH = 140f
        val left = x
        val top = y
        val right = x + stationW

        // 1. Draw glowing pickup zone on rightmost lane
        if (!isCollected) {
            val zoneLeft = roadRight - laneWidth
            val zoneRight = roadRight
            val zoneTop = y - 40f
            val zoneBottom = y + stationH + 40f
            val zoneRect = RectF(zoneLeft, zoneTop, zoneRight, zoneBottom)

            canvas.drawRoundRect(zoneRect, 12f, 12f, pickupZonePaint)
            canvas.drawRoundRect(zoneRect, 12f, 12f, pickupZoneBorderPaint)

            // Text on road: "قف هنا 🚌"
            val zoneTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("قف هنا 🚌", (zoneLeft + zoneRight) / 2f, (zoneTop + zoneBottom) / 2f + 6f, zoneTextPaint)
        }

        // 2. Station Shelter Canopy
        val shelterRect = RectF(left, top, right, top + 50f)
        canvas.drawRoundRect(shelterRect, 8f, 8f, shelterPaint)

        // 3. Station Sign Board
        val signRect = RectF(left - 10f, top - 35f, right + 10f, top + 5f)
        canvas.drawRoundRect(signRect, 6f, 6f, signBgPaint)
        canvas.drawRoundRect(signRect, 6f, 6f, signBorderPaint)
        canvas.drawText(name, (left + right) / 2f, top - 10f, textPaint)

        // 4. Commuter figures waiting at station (if not collected yet)
        if (!isCollected) {
            var commuterX = left + 20f
            val commuterY = top + 80f
            for (i in 0 until commuterCount) {
                // Commuter Head & Body
                canvas.drawCircle(commuterX, commuterY - 18f, 10f, commuterHeadPaint)
                val bodyRect = RectF(commuterX - 10f, commuterY - 6f, commuterX + 10f, commuterY + 25f)
                canvas.drawRoundRect(bodyRect, 4f, 4f, commuterShirtPaint)

                commuterX += 22f
            }
        }
    }
}

/**
 * Animated floating fare text popup ("+15 ج.م الأجرة").
 */
class FarePopup(
    val text: String,
    var x: Float,
    var y: Float
) {
    var alpha: Int = 255
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD54F") // Gold
        textSize = 42f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(8f, 2f, 2f, Color.BLACK)
    }

    fun update(): Boolean {
        y -= 2.5f
        alpha = (alpha - 5).coerceAtLeast(0)
        return alpha > 0
    }

    fun draw(canvas: Canvas) {
        textPaint.alpha = alpha
        canvas.drawText(text, x, y, textPaint)
    }
}
