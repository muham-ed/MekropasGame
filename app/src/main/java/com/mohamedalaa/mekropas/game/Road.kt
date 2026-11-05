package com.mohamedalaa.mekropas.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Handles road graphics rendering, lane positions, boundaries, and scrolling animation.
 */
class Road {
    var width: Float = 0f
        private set
    var height: Float = 0f
        private set

    var roadLeft: Float = 0f
        private set
    var roadRight: Float = 0f
        private set
    var roadWidth: Float = 0f
        private set

    val laneCount = 3
    private var scrollOffset = 0f
    var scrollSpeed = 16f

    // Paints
    private val shoulderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3B6E32") // Egyptian Delta green roadside grass/trees
    }
    private val desertEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C2B280") // Sand/dirt outer margin
    }
    private val asphaltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2B2D2F") // Dark asphalt road
    }
    private val edgeLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5D000") // Solid yellow highway edge lines
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }
    private val laneLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    private val curbRedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D32F2F")
    }
    private val curbWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val dashLength = 40f
    private val dashGap = 35f
    private val dashCycle = dashLength + dashGap

    fun updateSize(w: Float, h: Float) {
        width = w
        height = h

        // Road occupies ~75% of screen width in center, with 12.5% shoulders on each side
        val shoulderWidth = width * 0.12f
        roadLeft = shoulderWidth
        roadRight = width - shoulderWidth
        roadWidth = roadRight - roadLeft
    }

    fun update() {
        scrollOffset = (scrollOffset + scrollSpeed) % dashCycle
    }

    fun draw(canvas: Canvas) {
        if (width <= 0f || height <= 0f) return

        // 1. Draw outer desert margin
        canvas.drawRect(0f, 0f, width, height, desertEdgePaint)

        // 2. Draw green roadside shoulders
        val desertWidth = width * 0.03f
        canvas.drawRect(desertWidth, 0f, roadLeft, height, shoulderPaint)
        canvas.drawRect(roadRight, 0f, width - desertWidth, height, shoulderPaint)

        // 3. Draw Red & White Curb stripes along edges
        drawCurbs(canvas)

        // 4. Draw main Asphalt road surface
        canvas.drawRect(roadLeft, 0f, roadRight, height, asphaltPaint)

        // 5. Draw solid yellow outer road boundary lines
        canvas.drawLine(roadLeft, 0f, roadLeft, height, edgeLinePaint)
        canvas.drawLine(roadRight, 0f, roadRight, height, edgeLinePaint)

        // 6. Draw animated dashed white lane dividing lines
        val laneWidth = roadWidth / laneCount
        for (i in 1 until laneCount) {
            val laneX = roadLeft + i * laneWidth
            var y = -dashCycle + scrollOffset
            while (y < height + dashCycle) {
                canvas.drawLine(laneX, y, laneX, y + dashLength, laneLinePaint)
                y += dashCycle
            }
        }
    }

    private fun drawCurbs(canvas: Canvas) {
        val curbWidth = 16f
        val curbSegmentHeight = 60f
        var y = -curbSegmentHeight + (scrollOffset * 1.5f) % (curbSegmentHeight * 2)

        var isRed = true
        while (y < height + curbSegmentHeight) {
            val paint = if (isRed) curbRedPaint else curbWhitePaint

            // Left curb
            canvas.drawRect(roadLeft - curbWidth, y, roadLeft, y + curbSegmentHeight, paint)
            // Right curb
            canvas.drawRect(roadRight, y, roadRight + curbWidth, y + curbSegmentHeight, paint)

            y += curbSegmentHeight
            isRed = !isRed
        }
    }

    /**
     * Returns the center X coordinate for a given lane index (0 to laneCount-1).
     */
    fun getLaneCenterX(laneIndex: Int): Float {
        val clampedLane = laneIndex.coerceIn(0, laneCount - 1)
        val laneWidth = roadWidth / laneCount
        return roadLeft + (clampedLane + 0.5f) * laneWidth
    }
}
