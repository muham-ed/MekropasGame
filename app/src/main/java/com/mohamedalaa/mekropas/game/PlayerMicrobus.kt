package com.mohamedalaa.mekropas.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader

/**
 * Represents the player's Egyptian Microbus vehicle.
 * Handles movement physics, screen boundary clamping, horn honking visual effects, and detailed Canvas rendering.
 */
class PlayerMicrobus {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 110f
    var height: Float = 200f

    var speedMultiplier: Float = 1.0f

    var targetX: Float = 0f
    private var tiltAngle: Float = 0f

    var honkTimer: Int = 0

    val bounds: RectF = RectF()

    // Paints
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val redStripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E53935") // Primary stripe
    }
    private val blueStripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E88E5") // Secondary stripe
    }
    private val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#263238") // Dark tinted glass
    }
    private val windowHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 255, 255, 255)
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val tirePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1C1C")
    }
    private val headlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFEB3B")
    }
    private val lightBeamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val taillightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF1744")
    }
    private val roofRackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val luggagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FB8C00") // Orange roof luggage bag
    }
    private val licensePlatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ECEFF1")
    }
    private val licenseTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 14f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val hornWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD54F")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    fun applyVanModel(van: VanModel) {
        speedMultiplier = van.speedMultiplier
        try {
            bodyPaint.color = Color.parseColor(van.primaryColorHex)
            redStripePaint.color = Color.parseColor(van.stripeColorHex)
        } catch (e: Exception) {
            // Fallback
        }
    }

    fun initPosition(centerX: Float, bottomY: Float, vehicleWidth: Float = 110f, vehicleHeight: Float = 200f) {
        width = vehicleWidth
        height = vehicleHeight
        x = centerX
        targetX = centerX
        y = bottomY - height - 40f
        honkTimer = 0
        updateBounds()
    }

    fun honk() {
        honkTimer = 25
    }

    /**
     * Steer towards target position or steer continuously in direction (-1 to +1).
     */
    fun steer(direction: Float, roadLeft: Float, roadRight: Float) {
        val steerSpeed = 22f * speedMultiplier
        val minX = roadLeft + width / 2f
        val maxX = roadRight - width / 2f

        if (direction != 0f) {
            x += direction * steerSpeed
            tiltAngle = (direction * 6f).coerceIn(-8f, 8f)
        } else {
            // Smooth lerp to targetX if set
            val diff = targetX - x
            if (kotlin.math.abs(diff) > 2f) {
                x += diff * 0.25f
                tiltAngle = (diff * 0.05f).coerceIn(-8f, 8f)
            } else {
                tiltAngle *= 0.7f
            }
        }

        // Clamp to road boundaries
        x = x.coerceIn(minX, maxX)
        updateBounds()
    }

    private fun updateBounds() {
        bounds.set(
            x - width / 2f + 8f,
            y + 10f,
            x + width / 2f - 8f,
            y + height - 10f
        )
    }

    fun draw(canvas: Canvas) {
        canvas.save()

        // Apply tilt rotation around vehicle center
        canvas.rotate(tiltAngle, x, y + height / 2f)

        val halfW = width / 2f
        val left = x - halfW
        val top = y
        val right = x + halfW
        val bottom = y + height

        // 1. Draw Headlight Beams glowing ahead
        drawHeadlightBeams(canvas, left, top, right)

        // 2. Draw Horn Waves if honking
        if (honkTimer > 0) {
            honkTimer--
            drawHornWaves(canvas, top)
        }

        // 3. Vehicle Drop Shadow
        canvas.drawRoundRect(
            left - 6f, top + 15f, right + 6f, bottom + 12f,
            16f, 16f, shadowPaint
        )

        // 4. Rubber Tires
        val tireWidth = 14f
        val tireHeight = 36f
        // Front tires
        canvas.drawRoundRect(left - tireWidth / 2, top + 25f, left + tireWidth / 2, top + 25f + tireHeight, 6f, 6f, tirePaint)
        canvas.drawRoundRect(right - tireWidth / 2, top + 25f, right + tireWidth / 2, top + 25f + tireHeight, 6f, 6f, tirePaint)
        // Rear tires
        canvas.drawRoundRect(left - tireWidth / 2, bottom - 45f, left + tireWidth / 2, bottom - 45f + tireHeight, 6f, 6f, tirePaint)
        canvas.drawRoundRect(right - tireWidth / 2, bottom - 45f, right + tireWidth / 2, bottom - 45f + tireHeight, 6f, 6f, tirePaint)

        // 5. Main White Vehicle Body
        val bodyRect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(bodyRect, 22f, 22f, bodyPaint)

        // 6. Egyptian Microbus Decorative Side Stripes (Red & Blue)
        val stripeTop = top + height * 0.25f
        val stripeHeight = 12f
        canvas.drawRect(left + 2f, stripeTop, right - 2f, stripeTop + stripeHeight, redStripePaint)
        canvas.drawRect(left + 2f, stripeTop + stripeHeight, right - 2f, stripeTop + stripeHeight * 2, blueStripePaint)

        // 7. Front Windshield (Dark Tinted Glass)
        val windshieldTop = top + 18f
        val windshieldBottom = top + height * 0.28f
        val windshieldRect = RectF(left + 10f, windshieldTop, right - 10f, windshieldBottom)
        canvas.drawRoundRect(windshieldRect, 8f, 8f, windowPaint)

        // Glass reflection highlight
        canvas.drawLine(left + 18f, windshieldBottom - 6f, right - 18f, windshieldTop + 6f, windowHighlightPaint)

        // 8. Rear Glass Window
        val rearWindowRect = RectF(left + 12f, bottom - 30f, right - 12f, bottom - 12f)
        canvas.drawRoundRect(rearWindowRect, 6f, 6f, windowPaint)

        // 9. Side Window Strips
        val sideWindowLeft = RectF(left + 4f, top + height * 0.35f, left + 10f, bottom - 38f)
        val sideWindowRight = RectF(right - 10f, top + height * 0.35f, right - 4f, bottom - 38f)
        canvas.drawRoundRect(sideWindowLeft, 3f, 3f, windowPaint)
        canvas.drawRoundRect(sideWindowRight, 3f, 3f, windowPaint)

        // 10. Roof Luggage Rack with Luggage
        val rackTop = top + height * 0.42f
        val rackBottom = top + height * 0.72f
        val rackRect = RectF(left + 14f, rackTop, right - 14f, rackBottom)
        canvas.drawRoundRect(rackRect, 6f, 6f, roofRackPaint)
        val bagRect = RectF(left + 20f, rackTop + 8f, right - 20f, rackBottom - 8f)
        canvas.drawRoundRect(bagRect, 8f, 8f, luggagePaint)

        // 11. Front Headlights & Rear Taillights
        canvas.drawRoundRect(RectF(left + 6f, top + 2f, left + 22f, top + 12f), 4f, 4f, headlightPaint)
        canvas.drawRoundRect(RectF(right - 22f, top + 2f, right - 6f, top + 12f), 4f, 4f, headlightPaint)

        canvas.drawRoundRect(RectF(left + 6f, bottom - 10f, left + 20f, bottom - 2f), 3f, 3f, taillightPaint)
        canvas.drawRoundRect(RectF(right - 20f, bottom - 10f, right - 6f, bottom - 2f), 3f, 3f, taillightPaint)

        // 12. Egyptian License Plate on Rear Bumper
        val plateW = 44f
        val plateRect = RectF(x - plateW / 2f, bottom - 18f, x + plateW / 2f, bottom - 2f)
        canvas.drawRoundRect(plateRect, 4f, 4f, licensePlatePaint)
        canvas.drawText("المنوفية", x, bottom - 5f, licenseTextPaint)

        canvas.restore()
    }

    private fun drawHornWaves(canvas: Canvas, top: Float) {
        val waveY = top - 15f
        val progress = (25 - honkTimer) * 3f + 15f
        val r1 = progress
        val r2 = progress + 20f

        canvas.drawArc(RectF(x - r1, waveY - r1, x + r1, waveY + r1), 220f, 100f, false, hornWavePaint)
        canvas.drawArc(RectF(x - r2, waveY - r2, x + r2, waveY + r2), 220f, 100f, false, hornWavePaint)
    }

    private fun drawHeadlightBeams(canvas: Canvas, left: Float, top: Float, right: Float) {
        val beamLength = 220f
        val beamSpread = 40f

        val pathLeft = Path().apply {
            moveTo(left + 14f, top + 5f)
            lineTo(left - beamSpread, top - beamLength)
            lineTo(left + 30f + beamSpread / 2, top - beamLength)
            close()
        }

        val pathRight = Path().apply {
            moveTo(right - 14f, top + 5f)
            lineTo(right - 30f - beamSpread / 2, top - beamLength)
            lineTo(right + beamSpread, top - beamLength)
            close()
        }

        lightBeamPaint.shader = LinearGradient(
            x, top, x, top - beamLength,
            Color.argb(70, 255, 235, 59),
            Color.argb(0, 255, 235, 59),
            Shader.TileMode.CLAMP
        )

        canvas.drawPath(pathLeft, lightBeamPaint)
        canvas.drawPath(pathRight, lightBeamPaint)
    }
}
