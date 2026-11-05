package com.mohamedalaa.mekropas.game

import android.graphics.Canvas
import android.graphics.RectF
import kotlin.random.Random

/**
 * Manages dynamic spawning, downward movement, boundary cleanup,
 * and AABB collision detection for NPC traffic vehicles.
 */
class TrafficManager {
    val vehicles = mutableListOf<TrafficVehicle>()

    private var spawnTimer = 0
    private var spawnInterval = 80 // Frames between spawns (~1.3 seconds at 60 FPS)

    fun update(road: Road) {
        if (road.width <= 0f || road.height <= 0f) return

        // 1. Spawning Logic
        spawnTimer++
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0
            // Vary spawn interval randomly between 60 and 110 frames
            spawnInterval = Random.nextInt(60, 110)
            trySpawnVehicle(road)
        }

        // 2. Update and Clean Up Off-Screen Vehicles
        val iterator = vehicles.iterator()
        while (iterator.hasNext()) {
            val v = iterator.next()
            v.update(road.scrollSpeed)

            // Remove vehicle once completely past the bottom screen edge
            if (v.y > road.height + 150f) {
                iterator.remove()
            }
        }
    }

    private fun trySpawnVehicle(road: Road) {
        // Find candidate lanes that don't have a vehicle near the top spawn area
        val candidateLanes = mutableListOf<Int>()
        for (lane in 0 until road.laneCount) {
            val laneBlockedNearTop = vehicles.any { v ->
                v.laneIndex == lane && v.y < 350f
            }
            if (!laneBlockedNearTop) {
                candidateLanes.add(lane)
            }
        }

        if (candidateLanes.isNotEmpty()) {
            val chosenLane = candidateLanes.random()
            val type = VehicleType.entries.random()
            val spawnX = road.getLaneCenterX(chosenLane)

            // Calculate starting Y position based on vehicle height
            val tempVehicle = TrafficVehicle(type, chosenLane, spawnX, 0f)
            val startY = -tempVehicle.height - 20f
            tempVehicle.y = startY

            vehicles.add(tempVehicle)
        }
    }

    /**
     * Checks AABB bounding box collision between player microbus and all active traffic.
     */
    fun checkCollision(player: PlayerMicrobus): Boolean {
        val playerBounds = player.bounds
        for (v in vehicles) {
            if (RectF.intersects(playerBounds, v.bounds)) {
                return true
            }
        }
        return false
    }

    fun draw(canvas: Canvas) {
        for (v in vehicles) {
            v.draw(canvas)
        }
    }

    fun reset() {
        vehicles.clear()
        spawnTimer = 0
        spawnInterval = 80
    }
}
