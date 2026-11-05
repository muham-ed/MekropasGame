package com.mohamedalaa.mekropas.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

enum class VehicleType {
    TAXI,       // Yellow/Black Egyptian Taxi
    TRUCK,      // Heavy Cargo Truck
    TUKTUK,     // Black & Yellow Auto Rickshaw
    OTHER_MICROBUS // Red Microbus
}

/**
 * Represents an NPC traffic vehicle moving along the highway lanes.
 */
class TrafficVehicle(
    val type: VehicleType,
    val laneIndex: Int,
    startX: Float,
    startY: Float
) {
    var x: Float = startX
    var y: Float = startY

    var width: Float = 0f
    var height: Float = 0f
    var speed: Float = 0f

    val bounds: RectF = RectF()

    // Base Paints
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#212121") // Dark glass
    }
    private val tirePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#121212")
    }
    private val headlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFF59D")
    }
    private val taillightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D32F2F")
    }
    private val detailTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 12f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    init {
        when (type) {
            VehicleType.TAXI -> {
                width = 95f
                height = 175f
                speed = 6f
                bodyPaint.color = Color.parseColor("#FBC02D") // Yellow body
                secondaryPaint.color = Color.parseColor("#212121") // Black taxi stripes
            }
            VehicleType.TRUCK -> {
                width = 125f
                height = 240f
                speed = 3f
                bodyPaint.color = Color.parseColor("#388E3C") // Green cab
                secondaryPaint.color = Color.parseColor("#8D6E63") // Brown wooden cargo bed
            }
            VehicleType.TUKTUK -> {
                width = 80f
                height = 135f
                speed = 8f
                bodyPaint.color = Color.parseColor("#212121") // Black canopy
                secondaryPaint.color = Color.parseColor("#FBC02D") // Yellow frame
            }
            VehicleType.OTHER_MICROBUS -> {
                width = 105f
                height = 195f
                speed = 5f
                bodyPaint.color = Color.parseColor("#D32F2F") // Red Microbus
                secondaryPaint.color = Color.parseColor("#1976D2") // Blue stripe
            }
        }
        updateBounds()
    }

    fun update(roadScrollSpeed: Float) {
        // Traffic moves down relative to road speed + intrinsic vehicle speed
        y += speed + (roadScrollSpeed * 0.4f)
        updateBounds()
    }

    private fun updateBounds() {
        // AABB box with slight 6px inset for forgiving gameplay collision feel
        bounds.set(
            x - width / 2f + 6f,
            y + 8f,
            x + width / 2f - 6f,
            y + height - 8f
        )
    }

    fun draw(canvas: Canvas) {
        val halfW = width / 2f
        val left = x - halfW
        val top = y
        val right = x + halfW
        val bottom = y + height

        // Shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 0, 0, 0)
        }
        canvas.drawRoundRect(left - 4f, top + 10f, right + 4f, bottom + 8f, 12f, 12f, shadowPaint)

        when (type) {
            VehicleType.TAXI -> drawTaxi(canvas, left, top, right, bottom)
            VehicleType.TRUCK -> drawTruck(canvas, left, top, right, bottom)
            VehicleType.TUKTUK -> drawTuktuk(canvas, left, top, right, bottom)
            VehicleType.OTHER_MICROBUS -> drawOtherMicrobus(canvas, left, top, right, bottom)
        }
    }

    private fun drawTaxi(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        // Tires
        canvas.drawRoundRect(left - 4f, top + 20f, left + 8f, top + 50f, 4f, 4f, tirePaint)
        canvas.drawRoundRect(right - 8f, top + 20f, right + 4f, top + 50f, 4f, 4f, tirePaint)
        canvas.drawRoundRect(left - 4f, bottom - 50f, left + 8f, bottom - 20f, 4f, 4f, tirePaint)
        canvas.drawRoundRect(right - 8f, bottom - 50f, right + 4f, bottom - 20f, 4f, 4f, tirePaint)

        // Yellow Body
        val bodyRect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(bodyRect, 18f, 18f, bodyPaint)

        // Black Taxi side stripes
        canvas.drawRect(left + 2f, top + height * 0.35f, left + 10f, bottom - height * 0.25f, secondaryPaint)
        canvas.drawRect(right - 10f, top + height * 0.35f, right - 2f, bottom - height * 0.25f, secondaryPaint)

        // Windshield and rear glass
        canvas.drawRoundRect(RectF(left + 8f, top + 15f, right - 8f, top + 48f), 6f, 6f, windowPaint)
        canvas.drawRoundRect(RectF(left + 10f, bottom - 30f, right - 10f, bottom - 10f), 5f, 5f, windowPaint)

        // Taxi Roof Sign "تاكسي"
        val signRect = RectF(x - 22f, top + height * 0.45f, x + 22f, top + height * 0.58f)
        canvas.drawRoundRect(signRect, 4f, 4f, secondaryPaint)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.YELLOW
            textSize = 10f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("تاكسي", x, top + height * 0.54f, textPaint)

        // Lights
        canvas.drawRoundRect(RectF(left + 4f, top + 2f, left + 18f, top + 10f), 3f, 3f, headlightPaint)
        canvas.drawRoundRect(RectF(right - 18f, top + 2f, right - 4f, top + 10f), 3f, 3f, headlightPaint)
        canvas.drawRoundRect(RectF(left + 4f, bottom - 8f, left + 16f, bottom - 2f), 3f, 3f, taillightPaint)
        canvas.drawRoundRect(RectF(right - 16f, bottom - 8f, right - 4f, bottom - 2f), 3f, 3f, taillightPaint)
    }

    private fun drawTruck(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        // Wooden Cargo Bed at top/rear
        val bedTop = top + 70f
        canvas.drawRoundRect(RectF(left, bedTop, right, bottom), 8f, 8f, secondaryPaint)

        // Front Cab
        val cabRect = RectF(left + 4f, top, right - 4f, bedTop - 5f)
        canvas.drawRoundRect(cabRect, 14f, 14f, bodyPaint)

        // Cab Windshield
        canvas.drawRoundRect(RectF(left + 10f, top + 12f, right - 10f, top + 42f), 6f, 6f, windowPaint)

        // Heavy dual rear tires
        canvas.drawRoundRect(left - 6f, bottom - 60f, left + 6f, bottom - 10f, 4f, 4f, tirePaint)
        canvas.drawRoundRect(right - 6f, bottom - 60f, right + 6f, bottom - 10f, 4f, 4f, tirePaint)

        // Lights
        canvas.drawRoundRect(RectF(left + 8f, top + 2f, left + 22f, top + 10f), 3f, 3f, headlightPaint)
        canvas.drawRoundRect(RectF(right - 22f, top + 2f, right - 8f, top + 10f), 3f, 3f, headlightPaint)
        canvas.drawRoundRect(RectF(left + 6f, bottom - 10f, left + 20f, bottom - 2f), 3f, 3f, taillightPaint)
        canvas.drawRoundRect(RectF(right - 20f, bottom - 10f, right - 6f, bottom - 2f), 3f, 3f, taillightPaint)
    }

    private fun drawTuktuk(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        // Single front wheel
        canvas.drawRoundRect(x - 5f, top, x + 5f, top + 20f, 3f, 3f, tirePaint)
        // Rear wheels
        canvas.drawRoundRect(left - 4f, bottom - 30f, left + 4f, bottom - 5f, 3f, 3f, tirePaint)
        canvas.drawRoundRect(right - 4f, bottom - 30f, right + 4f, bottom - 5f, 3f, 3f, tirePaint)

        // Canopy Body
        val canopyRect = RectF(left, top + 15f, right, bottom)
        canvas.drawRoundRect(canopyRect, 12f, 12f, bodyPaint)

        // Front windshield
        canvas.drawRoundRect(RectF(left + 6f, top + 20f, right - 6f, top + 45f), 4f, 4f, windowPaint)

        // Yellow frame accents
        canvas.drawRect(left + 2f, top + 50f, right - 2f, top + 56f, secondaryPaint)

        // Single central headlight
        canvas.drawCircle(x, top + 16f, 7f, headlightPaint)
        canvas.drawRoundRect(RectF(left + 4f, bottom - 6f, right - 4f, bottom - 2f), 2f, 2f, taillightPaint)
    }

    private fun drawOtherMicrobus(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        // Red Microbus Body
        val bodyRect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(bodyRect, 20f, 20f, bodyPaint)

        // Side blue stripe
        canvas.drawRect(left + 2f, top + height * 0.3f, right - 2f, top + height * 0.38f, secondaryPaint)

        // Windshield and rear window
        canvas.drawRoundRect(RectF(left + 8f, top + 16f, right - 8f, top + 50f), 6f, 6f, windowPaint)
        canvas.drawRoundRect(RectF(left + 10f, bottom - 28f, right - 10f, bottom - 10f), 5f, 5f, windowPaint)

        // Lights
        canvas.drawRoundRect(RectF(left + 6f, top + 2f, left + 20f, top + 10f), 3f, 3f, headlightPaint)
        canvas.drawRoundRect(RectF(right - 20f, top + 2f, right - 6f, top + 10f), 3f, 3f, headlightPaint)
        canvas.drawRoundRect(RectF(left + 6f, bottom - 8f, left + 18f, bottom - 2f), 3f, 3f, taillightPaint)
        canvas.drawRoundRect(RectF(right - 18f, bottom - 8f, right - 6f, bottom - 2f), 3f, 3f, taillightPaint)
    }
}
